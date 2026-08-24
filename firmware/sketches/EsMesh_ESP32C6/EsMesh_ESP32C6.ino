/*
 * ==============================================================================
 *  EsMesh Node Firmware - ESP32-C6 Target (Arduino IDE Sketch)
 * ==============================================================================
 *  Board: ESP32C6 Dev Module / ESP32-C6-DevKitC-1
 *  Settings:
 *    - USB CDC On Boot: Enabled
 *    - Partition Scheme: Minimal SPIFFS / Default
 *
 *  Required Libraries:
 *    - painlessMesh
 *    - ArduinoJson (v6.x)
 *    - ESPAsyncWebServer
 *    - AsyncTCP
 * ==============================================================================
 */

#define ESMESH_CHIP_ESP32C6 1
#define ESMESH_FIRMWARE_VERSION "1.0.0"
#define ESMESH_DEFAULT_PORT 80
#define ESMESH_UDP_BEACON_PORT 8266
#define ESMESH_WEBSOCKET_PATH "/ws"

#include <Arduino.h>
#include <ArduinoJson.h>
#include <Preferences.h>
#include <WiFi.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <painlessMesh.h>
#include <ESPmDNS.h>
#include <WiFiUdp.h>

struct EsMeshNodeConfig {
    String nodeName = "ESM-C6";
    String meshPrefix = "EsMesh_RF_Net";
    String meshPassword = "EsMeshSecurePassword123";
    uint16_t meshPort = 5555;
    uint8_t meshChannel = 6;
    bool isGateway = false;
    String wifiSsid = "";
    String wifiPassword = "";
    bool telemetryEnabled = true;
    uint32_t telemetryIntervalMs = 2000;
};

Scheduler userScheduler;
painlessMesh mesh;
AsyncWebServer server(ESMESH_DEFAULT_PORT);
AsyncWebSocket ws(ESMESH_WEBSOCKET_PATH);
WiFiUDP udpBeacon;
Preferences preferences;

EsMeshNodeConfig config;
String localNodeIdStr = "";
unsigned long lastTelemetryMillis = 0;
unsigned long lastBeaconMillis = 0;

void setupWebServer();
void sendTelemetryBroadcast();
void sendUdpDiscoveryBeacon();
void loadConfiguration();
void saveConfiguration();

void receivedCallback(uint32_t from, String &msg) {
    Serial.printf("[Mesh RX] From: 0x%08X Msg: %s\n", from, msg.c_str());
    StaticJsonDocument<1024> doc;
    if (!deserializeJson(doc, msg)) {
        doc["rxFrom"] = String(from, HEX);
        String output;
        serializeJson(doc, output);
        ws.textAll(output);
    } else {
        StaticJsonDocument<512> textDoc;
        textDoc["type"] = "mesh_message";
        textDoc["senderId"] = String(from, HEX);
        textDoc["text"] = msg;
        textDoc["timestamp"] = millis();
        String out;
        serializeJson(textDoc, out);
        ws.textAll(out);
    }
}

void newConnectionCallback(uint32_t nodeId) {
    Serial.printf("[Mesh] New Connection: 0x%08X\n", nodeId);
}

void changedConnectionCallback() {
    Serial.printf("[Mesh] Topology Changed. Nodes: %u\n", mesh.getNodeList().size() + 1);
    StaticJsonDocument<512> doc;
    doc["type"] = "topology_update";
    doc["nodeCount"] = mesh.getNodeList().size() + 1;
    doc["gatewayNodeId"] = localNodeIdStr;
    String out;
    serializeJson(doc, out);
    ws.textAll(out);
}

void setup() {
    Serial.begin(115200);
    delay(500);

    Serial.println("\n==========================================");
    Serial.printf("  EsMesh Node Firmware v%s\n", ESMESH_FIRMWARE_VERSION);
    Serial.println("  Target: ESP32-C6 (WiFi 6 / Thread / BLE5)");
    Serial.println("==========================================\n");

    loadConfiguration();

    mesh.setDebugMsgTypes(ERROR | STARTUP | CONNECTION);
    mesh.init(config.meshPrefix, config.meshPassword, &userScheduler, config.meshPort, WIFI_AP_STA, config.meshChannel);
    mesh.onReceive(&receivedCallback);
    mesh.onNewConnection(&newConnectionCallback);
    mesh.onChangedConnections(&changedConnectionCallback);

    localNodeIdStr = String(mesh.getNodeId(), HEX);
    localNodeIdStr.toUpperCase();

    if (config.isGateway && config.wifiSsid.length() > 0) {
        Serial.printf("[Gateway] Connecting to router SSID: %s\n", config.wifiSsid.c_str());
        mesh.stationManual(config.wifiSsid, config.wifiPassword);
    }

    if (MDNS.begin("esmesh-c6")) {
        MDNS.addService("esmesh", "tcp", ESMESH_DEFAULT_PORT);
        MDNS.addService("http", "tcp", ESMESH_DEFAULT_PORT);
    }

    udpBeacon.begin(ESMESH_UDP_BEACON_PORT);
    setupWebServer();

    Serial.printf("[EsMesh-C6] Node ID: 0x%08X ready.\n", mesh.getNodeId());
}

void loop() {
    mesh.update();
    ws.cleanupClients();

    unsigned long currentMillis = millis();

    if (config.telemetryEnabled && (currentMillis - lastTelemetryMillis >= config.telemetryIntervalMs)) {
        lastTelemetryMillis = currentMillis;
        sendTelemetryBroadcast();
    }

    if (currentMillis - lastBeaconMillis >= 3000) {
        lastBeaconMillis = currentMillis;
        sendUdpDiscoveryBeacon();
    }
}

void sendUdpDiscoveryBeacon() {
    IPAddress broadcastIp(255, 255, 255, 255);
    StaticJsonDocument<256> beaconDoc;
    beaconDoc["service"] = "esmesh";
    beaconDoc["nodeId"] = localNodeIdStr;
    beaconDoc["nodeName"] = config.nodeName;
    beaconDoc["chipModel"] = "ESP32-C6";
    beaconDoc["version"] = ESMESH_FIRMWARE_VERSION;
    beaconDoc["port"] = ESMESH_DEFAULT_PORT;
    beaconDoc["isGateway"] = config.isGateway;

    String beaconStr;
    serializeJson(beaconDoc, beaconStr);

    udpBeacon.beginPacket(broadcastIp, ESMESH_UDP_BEACON_PORT);
    udpBeacon.write((const uint8_t*)beaconStr.c_str(), beaconStr.length());
    udpBeacon.endPacket();
}

void sendTelemetryBroadcast() {
    StaticJsonDocument<512> doc;
    doc["type"] = "telemetry";
    doc["nodeId"] = localNodeIdStr;
    doc["nodeName"] = config.nodeName;
    doc["rssi"] = WiFi.RSSI();
    doc["freeHeap"] = ESP.getFreeHeap();
    doc["uptimeSeconds"] = millis() / 1000;
    doc["meshNodes"] = mesh.getNodeList().size() + 1;
    doc["chipModel"] = "ESP32-C6";

    String jsonStr;
    serializeJson(doc, jsonStr);
    if (ws.count() > 0) {
        ws.textAll(jsonStr);
    }
}

void setupWebServer() {
    server.on("/api/status", HTTP_GET, [](AsyncWebServerRequest *request) {
        StaticJsonDocument<512> doc;
        doc["nodeId"] = localNodeIdStr;
        doc["nodeName"] = config.nodeName;
        doc["chipModel"] = "ESP32-C6";
        doc["firmwareVersion"] = ESMESH_FIRMWARE_VERSION;
        doc["freeHeap"] = ESP.getFreeHeap();
        doc["totalHeap"] = ESP.getHeapSize();
        doc["rssi"] = WiFi.RSSI();
        doc["uptime"] = millis() / 1000;
        doc["isGateway"] = config.isGateway;
        doc["connectedNodes"] = mesh.getNodeList().size();
        String res;
        serializeJson(doc, res);
        request->send(200, "application/json", res);
    });

    server.on("/api/nodes", HTTP_GET, [](AsyncWebServerRequest *request) {
        StaticJsonDocument<2048> doc;
        doc["gatewayNodeId"] = localNodeIdStr;
        doc["totalNodes"] = mesh.getNodeList().size() + 1;
        doc["routerSsid"] = config.wifiSsid;
        doc["gatewayIp"] = WiFi.localIP().toString();

        JsonArray nodesArray = doc.createNestedArray("nodes");
        JsonObject selfObj = nodesArray.createNestedObject();
        selfObj["nodeId"] = localNodeIdStr;
        selfObj["name"] = config.nodeName;
        selfObj["role"] = config.isGateway ? "Gateway" : "Mesh Root";
        selfObj["ip"] = WiFi.localIP().toString();
        selfObj["rssi"] = WiFi.RSSI();
        selfObj["hops"] = 0;

        auto nodes = mesh.getNodeList();
        for (auto &&nodeId : nodes) {
            JsonObject nodeObj = nodesArray.createNestedObject();
            char hexId[12];
            snprintf(hexId, sizeof(hexId), "%08X", nodeId);
            nodeObj["nodeId"] = String(hexId);
            nodeObj["name"] = "Node-" + String(hexId).substring(4);
            nodeObj["role"] = "Mesh Node";
            nodeObj["hops"] = 1;
        }
        String res;
        serializeJson(doc, res);
        request->send(200, "application/json", res);
    });

    server.on("/api/config", HTTP_POST, [](AsyncWebServerRequest *request) {}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            StaticJsonDocument<512> doc;
            if (!deserializeJson(doc, data, len)) {
                if (doc.containsKey("nodeName")) config.nodeName = doc["nodeName"].as<String>();
                if (doc.containsKey("isGateway")) config.isGateway = doc["isGateway"].as<bool>();
                if (doc.containsKey("wifiSsid")) config.wifiSsid = doc["wifiSsid"].as<String>();
                if (doc.containsKey("wifiPassword")) config.wifiPassword = doc["wifiPassword"].as<String>();
                saveConfiguration();
                request->send(200, "application/json", "{\"status\":\"success\"}");
                delay(500);
                ESP.restart();
            } else {
                request->send(400, "application/json", "{\"status\":\"error\"}");
            }
        }
    );

    server.on("/api/send", HTTP_POST, [](AsyncWebServerRequest *request) {}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            StaticJsonDocument<512> doc;
            if (!deserializeJson(doc, data, len)) {
                String text = doc["text"].as<String>();
                String targetNodeHex = doc["target"].as<String>();
                if (targetNodeHex.length() > 0 && targetNodeHex != "broadcast" && targetNodeHex != "ALL") {
                    uint32_t targetId = strtoul(targetNodeHex.c_str(), NULL, 16);
                    mesh.sendSingle(targetId, text);
                } else {
                    mesh.sendBroadcast(text);
                }
                request->send(200, "application/json", "{\"status\":\"sent\"}");
            } else {
                request->send(400, "application/json", "{\"status\":\"error\"}");
            }
        }
    );

    server.on("/api/reboot", HTTP_POST, [](AsyncWebServerRequest *request) {
        request->send(200, "application/json", "{\"status\":\"rebooting\"}");
        delay(500);
        ESP.restart();
    });

    ws.onEvent([](AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
        if (type == WS_EVT_DATA) {
            AwsFrameInfo *info = (AwsFrameInfo*)arg;
            if (info->final && info->index == 0 && info->len == len && info->opcode == WS_TEXT) {
                data[len] = 0;
                mesh.sendBroadcast((char*)data);
            }
        }
    });

    server.addHandler(&ws);
    server.begin();
}

void loadConfiguration() {
    preferences.begin("esmesh", true);
    config.nodeName = preferences.getString("name", "ESM-C6");
    config.meshPrefix = preferences.getString("prefix", "EsMesh_RF_Net");
    config.meshPassword = preferences.getString("pass", "EsMeshSecurePassword123");
    config.meshChannel = preferences.getUChar("channel", 6);
    config.isGateway = preferences.getBool("is_gw", false);
    config.wifiSsid = preferences.getString("wifi_ssid", "");
    config.wifiPassword = preferences.getString("wifi_pass", "");
    preferences.end();
}

void saveConfiguration() {
    preferences.begin("esmesh", false);
    preferences.putString("name", config.nodeName);
    preferences.putString("prefix", config.meshPrefix);
    preferences.putString("pass", config.meshPassword);
    preferences.putUChar("channel", config.meshChannel);
    preferences.putBool("is_gw", config.isGateway);
    preferences.putString("wifi_ssid", config.wifiSsid);
    preferences.putString("wifi_pass", config.wifiPassword);
    preferences.end();
}
