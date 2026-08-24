package com.example.ui.viewmodel

import android.content.Context
import android.hardware.usb.UsbDevice
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.flasher.FlashBackend
import com.example.data.flasher.FlashStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FlasherUiState(
    val selectedFileUri: Uri? = null,
    val fileName: String? = null,
    val fileSizeFormatted: String? = null,
    val chipTarget: String? = null,
    val detectedUsbDevices: List<UsbDevice> = emptyList(),
    val selectedUsbDevice: UsbDevice? = null,
    val baudRate: Int = 460800,
    val flashStatus: FlashStatus = FlashStatus.Idle,
    val progressPercent: Int = 0,
    val currentStage: String = "Idle"
)

class FlasherViewModel(
    private val flashBackend: FlashBackend
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlasherUiState())
    val uiState: StateFlow<FlasherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            flashBackend.status.collect { status ->
                val progress = when (status) {
                    is FlashStatus.Flashing -> status.progressPercent
                    is FlashStatus.Completed -> 100
                    else -> 0
                }
                val stage = when (status) {
                    is FlashStatus.Idle -> "READY"
                    is FlashStatus.FileValidated -> "Binary Validated (${status.chipTarget})"
                    is FlashStatus.UsbDetected -> "USB Detected (${status.deviceName})"
                    is FlashStatus.RequestingPermission -> "Requesting USB OTG Permission..."
                    is FlashStatus.PermissionDenied -> "USB Permission Denied"
                    is FlashStatus.Connecting -> "Connecting to ESP32 Serial Bootloader..."
                    is FlashStatus.Flashing -> status.stage
                    is FlashStatus.Completed -> status.message
                    is FlashStatus.Failed -> "Failed: ${status.error}"
                    is FlashStatus.Cancelled -> "Flashing Cancelled"
                }

                _uiState.value = _uiState.value.copy(
                    flashStatus = status,
                    progressPercent = progress,
                    currentStage = stage
                )
            }
        }
    }

    fun onFileSelected(context: Context, uri: Uri) {
        val result = flashBackend.validateBinary(context, uri)
        result.onSuccess { (name, size, chip) ->
            val sizeKb = size / 1024
            val sizeFormatted = if (sizeKb > 1024) String.format("%.2f MB", sizeKb / 1024.0) else "$sizeKb KB"
            _uiState.value = _uiState.value.copy(
                selectedFileUri = uri,
                fileName = name,
                fileSizeFormatted = sizeFormatted,
                chipTarget = chip
            )
        }.onFailure { err ->
            _uiState.value = _uiState.value.copy(
                selectedFileUri = null,
                fileName = null,
                currentStage = "Invalid binary: ${err.localizedMessage}"
            )
        }
    }

    fun detectUsbDevices(context: Context) {
        val devices = flashBackend.detectUsbDevices(context)
        _uiState.value = _uiState.value.copy(
            detectedUsbDevices = devices,
            selectedUsbDevice = devices.firstOrNull()
        )
    }

    fun selectUsbDevice(device: UsbDevice) {
        _uiState.value = _uiState.value.copy(selectedUsbDevice = device)
    }

    fun setBaudRate(baud: Int) {
        _uiState.value = _uiState.value.copy(baudRate = baud)
    }

    fun flashFirmware(context: Context) {
        val uri = _uiState.value.selectedFileUri ?: return
        val usb = _uiState.value.selectedUsbDevice ?: return
        val baud = _uiState.value.baudRate

        viewModelScope.launch {
            flashBackend.flashFirmware(context, uri, usb, baud)
        }
    }

    fun cancelFlashing() {
        flashBackend.cancelFlashing()
    }

    class Factory(private val backend: FlashBackend) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FlasherViewModel(backend) as T
        }
    }
}
