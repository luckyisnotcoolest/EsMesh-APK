# EsMesh ESP32 Firmware Suite (v1.0.0)

Production-ready firmware for ESP32 mesh nodes and gateways, engineered to work seamlessly with the **EsMesh Android App**.

---

## 🌟 Key Features

1. **Auto-Merged Single Binary Output**:
   - Automated via `merge_firmware.py` using `esptool merge_bin`.
   - Bootloader (`0x0000`/`0x1000`), partition table (`0x8000`), and application firmware (`0x10000`) are packaged into a single `esmesh_<chip>_merged.bin` file ready for flashing at `0x00000`.
   - Can be directly flashed via the **EsMesh Android App USB Flasher** or WebUSB/esptool.

2. **Supported Hardware Targets**:
   - **ESP32-S3**: `esp32s3_node` (Native USB CDC / JTAG + 8MB Flash)
   - **ESP32 Classic / WROOM / WROVER**: `esp32_classic_node`
   - **ESP32-C3**: `esp32c3_node` (RISC-V Single Core)
   - **ESP32-C6**: `esp32c6_node` (RISC-V + 802.15.4 / WiFi 6)

3. **Built-in Mesh & Networking Protocol**:
   - **painlessMesh 2.4GHz RF**: Self-organizing, self-healing mesh topology.
   - **HTTP REST API**: `/api/status`, `/api/nodes`, `/api/config`, `/api/send`, `/api/reboot`.
   - **WebSocket Streaming**: `/ws` real-time bidirectional telemetry & chat packets.
   - **mDNS & UDP Discovery Beacons**: Broadcasts on port `8266` for instant zero-config pairing in the Android app.
   - **NVS Persistence**: Stores node name, mesh credentials, and gateway Wi-Fi settings across reboots.

---

## 🚀 Building the Firmwares

### Prerequisites
- Python 3.8+
- [PlatformIO CLI](https://platformio.org/)

```bash
pip install -U platformio esptool
```

### Build Targets

```bash
cd firmware

# 1. Build & Merge ESP32-S3 Firmware:
pio run -e esp32s3_node

# 2. Build & Merge ESP32 Classic Firmware:
pio run -e esp32_classic_node

# 3. Build & Merge ESP32-C3 Firmware:
pio run -e esp32c3_node

# 4. Build & Merge ESP32-C6 Firmware:
pio run -e esp32c6_node
```

The merged output `.bin` files will be automatically generated and placed in `firmware/dist/`:
- `firmware/dist/esmesh_esp32s3_merged.bin`
- `firmware/dist/esmesh_esp32_merged.bin`
- `firmware/dist/esmesh_esp32c3_merged.bin`
- `firmware/dist/esmesh_esp32c6_merged.bin`

---

## ⚡ Flashing to ESP32

### Option 1: Directly via Android Phone (EsMesh-APK)
1. Plug your ESP32-S3 / ESP32 board into your Android phone via USB OTG cable.
2. Open **EsMesh App** -> Navigate to **Flasher**.
3. Select the `esmesh_esp32s3_merged.bin` file.
4. Select your connected USB device and tap **FLASH FIRMWARE**.

### Option 2: Via Command Line (esptool)
```bash
esptool.py --chip esp32s3 -b 921600 write_flash 0x0 dist/esmesh_esp32s3_merged.bin
```
