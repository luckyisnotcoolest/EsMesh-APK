# EsMesh ESP32 Firmware Flasher Specification

## 1. USB OTG Flashing Architecture
The Android application connects directly to ESP32 nodes via USB-C OTG cables using the `android.hardware.usb.UsbManager` framework.

### Supported USB-UART Bridge Chips:
- **Espressif Native USB-JTAG/CDC** (Vendor ID: `0x303A`)
- **Silicon Labs CP2102/CP2104** (Vendor ID: `0x10C4`)
- **WCH CH340 / CH341** (Vendor ID: `0x1A86`)
- **FTDI FT232R** (Vendor ID: `0x0403`)

---

## 2. Firmware Validation Rules
1. Must contain valid ESP bootloader magic byte (`0xE9` at offset 0).
2. Size must be at least 32 bytes and under max flash capacity.
3. Checksum verification before execution.

---

## 3. Flashing Process State Machine
1. `Idle` → `FileValidated` → `UsbDetected`
2. `RequestingPermission` (Android system prompt)
3. `Connecting` (Bootloader synchronization at selected Baud: 460800 or 115200)
4. `Flashing` (Sector erase + chunk transfer 4KB blocks)
5. `Completed` (Checksum verify & node reboot)
