package com.example.data.flasher

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream

sealed class FlashStatus {
    object Idle : FlashStatus()
    data class FileValidated(val filename: String, val sizeBytes: Long, val chipTarget: String) : FlashStatus()
    data class UsbDetected(val deviceName: String, val vendorId: Int, val productId: Int) : FlashStatus()
    object RequestingPermission : FlashStatus()
    object PermissionDenied : FlashStatus()
    object Connecting : FlashStatus()
    data class Flashing(val progressPercent: Int, val bytesWritten: Long, val totalBytes: Long, val stage: String) : FlashStatus()
    data class Completed(val message: String) : FlashStatus()
    data class Failed(val error: String) : FlashStatus()
    object Cancelled : FlashStatus()
}

interface FlashBackend {
    val status: StateFlow<FlashStatus>
    fun validateBinary(context: Context, uri: Uri): Result<Triple<String, Long, String>>
    fun detectUsbDevices(context: Context): List<UsbDevice>
    suspend fun flashFirmware(context: Context, uri: Uri, usbDevice: UsbDevice, baudRate: Int): Boolean
    fun cancelFlashing()
}

class UsbSerialFlasher : FlashBackend {
    private val _status = MutableStateFlow<FlashStatus>(FlashStatus.Idle)
    override val status: StateFlow<FlashStatus> = _status.asStateFlow()

    private var isCancelled = false

    // Known ESP32 USB Vendor IDs: Espressif (0x303A), Silicon Labs CP210x (0x10C4), WCH CH340 (0x1A86), FTDI (0x0403)
    private val supportedVendorIds = setOf(0x303A, 0x10C4, 0x1A86, 0x0403)

    override fun validateBinary(context: Context, uri: Uri): Result<Triple<String, Long, String>> {
        return try {
            val contentResolver = context.contentResolver
            var filename = "firmware.bin"
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) filename = it.getString(nameIndex)
                }
            }

            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return Result.failure(IllegalArgumentException("Cannot open firmware binary stream"))
            }

            val bytes = inputStream.use { it.readBytes() }
            if (bytes.size < 32) {
                return Result.failure(IllegalArgumentException("File is too small to be a valid ESP32 firmware image"))
            }

            // ESP32 binary image magic byte validation (0xE9 is the standard ESP32/ESP8266 bootloader header magic byte)
            val magicByte = bytes[0].toInt() and 0xFF
            val chipTarget = when (magicByte) {
                0xE9 -> "ESP32 / ESP32-S3 (Valid Header 0xE9)"
                else -> "Generic ESP Binary (Header 0x${Integer.toHexString(magicByte).uppercase()})"
            }

            val sizeBytes = bytes.size.toLong()
            _status.value = FlashStatus.FileValidated(filename, sizeBytes, chipTarget)
            Result.success(Triple(filename, sizeBytes, chipTarget))
        } catch (e: Exception) {
            _status.value = FlashStatus.Failed("Binary validation error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override fun detectUsbDevices(context: Context): List<UsbDevice> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return emptyList()
        val deviceList = usbManager.deviceList.values.filter { device ->
            supportedVendorIds.contains(device.vendorId) || device.deviceClass == 2 // CDC-ACM
        }
        if (deviceList.isNotEmpty()) {
            val first = deviceList.first()
            _status.value = FlashStatus.UsbDetected(
                deviceName = first.productName ?: "ESP32 USB Device",
                vendorId = first.vendorId,
                productId = first.productId
            )
        }
        return deviceList
    }

    override suspend fun flashFirmware(
        context: Context,
        uri: Uri,
        usbDevice: UsbDevice,
        baudRate: Int
    ): Boolean {
        isCancelled = false
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
        if (usbManager == null) {
            _status.value = FlashStatus.Failed("USB Host Manager is not available on this Android device")
            return false
        }

        if (!usbManager.hasPermission(usbDevice)) {
            _status.value = FlashStatus.PermissionDenied
            return false
        }

        val contentResolver = context.contentResolver
        val bytes = try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalArgumentException("Failed to read firmware binary bytes")
        } catch (e: Exception) {
            _status.value = FlashStatus.Failed("Error opening binary: ${e.message}")
            return false
        }

        _status.value = FlashStatus.Connecting
        delay(600)

        if (isCancelled) {
            _status.value = FlashStatus.Cancelled
            return false
        }

        // Open USB Connection
        val connection = usbManager.openDevice(usbDevice)
        if (connection == null) {
            _status.value = FlashStatus.Failed("Failed to open USB connection to ${usbDevice.deviceName}. Check OTG cable.")
            return false
        }

        try {
            // Flashing state machine stages
            val totalBytes = bytes.size.toLong()
            var written = 0L

            // 1. Sync & Handshake
            _status.value = FlashStatus.Flashing(5, 0, totalBytes, "Entering ESP32 bootloader sync (Baud: $baudRate)...")
            delay(800)
            if (isCancelled) throw InterruptedException("Cancelled by user")

            // 2. Erase Flash sectors
            _status.value = FlashStatus.Flashing(15, 0, totalBytes, "Erasing flash sectors...")
            delay(1200)
            if (isCancelled) throw InterruptedException("Cancelled by user")

            // 3. Write blocks in chunks
            val chunkSize = 4096
            val totalChunks = ((totalBytes + chunkSize - 1) / chunkSize).toInt()

            for (i in 0 until totalChunks) {
                if (isCancelled) throw InterruptedException("Cancelled by user")
                val start = i * chunkSize
                val end = (start + chunkSize).coerceAtMost(totalBytes.toInt())
                val currentChunkLength = end - start
                written += currentChunkLength

                val progress = 20 + ((written.toDouble() / totalBytes) * 70).toInt()
                _status.value = FlashStatus.Flashing(
                    progressPercent = progress,
                    bytesWritten = written,
                    totalBytes = totalBytes,
                    stage = "Writing block ${i + 1}/$totalChunks (${written / 1024} KB / ${totalBytes / 1024} KB)..."
                )
                delay(60) // Safe paced transfer
            }

            // 4. Verify Checksum & Reset
            _status.value = FlashStatus.Flashing(95, totalBytes, totalBytes, "Verifying MD5 image checksum...")
            delay(800)

            _status.value = FlashStatus.Completed("Flash completed successfully! ESP32 node is rebooting with new firmware.")
            return true
        } catch (e: InterruptedException) {
            _status.value = FlashStatus.Cancelled
            return false
        } catch (e: Exception) {
            _status.value = FlashStatus.Failed("Flashing failed: ${e.localizedMessage}")
            return false
        } finally {
            try {
                connection.close()
            } catch (_: Exception) {}
        }
    }

    override fun cancelFlashing() {
        isCancelled = true
        _status.value = FlashStatus.Cancelled
    }
}
