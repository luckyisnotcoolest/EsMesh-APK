#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <painlessMesh.h>
#include <ESPmDNS.h>
#include <WiFiUdp.h>

#define ESMESH_FIRMWARE_VERSION "1.0.0"
#define ESMESH_DEFAULT_PORT 80
#define ESMESH_UDP_BEACON_PORT 8266
#define ESMESH_WEBSOCKET_PATH "/ws"

struct EsMeshNodeConfig {
    String nodeName = "EsMesh-Node";
    String meshPrefix = "EsMesh_RF_Net";
    String meshPassword = "EsMeshSecurePassword123";
    uint16_t meshPort = 5555;
    uint8_t meshChannel = 6;
    bool isGateway = false;
    String wifiSsid = "";
    String wifiPassword = "";
    int8_t txPowerDbm = 20;
    bool telemetryEnabled = true;
    uint32_t telemetryIntervalMs = 2000;
};
