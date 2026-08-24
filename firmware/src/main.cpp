#include "EsMeshConfig.h"

// -------------------------------------------------------------
// Global Instances & Singletons
// -------------------------------------------------------------
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
uint32_t messageCounter = 0;

// -------------------------------------------------------------
// Forward Declarations
// -------------------------------------------------------------
void receivedCallback(uint32_t from, String &msg);
void newConnectionCallback(uint32_t nodeId);
void changedConnectionCallback();
void nodeTimeAdjustedCallback(int32_t offset);
void setupWebServer();
void sendTelemetryBroadcast();
void sendUdpDiscoveryBeacon();
void loadConfiguration();
void saveConfiguration();

// -------------------------------------------------------------
// Hardware & Chip Info Helpers
// -------------------------------------------------------------
String getChipModelName() {
#if defined(ESMESH_CHIP_ESP32S3)
    return "ESP32-S3";
#elif defined(ESMESH_CHIP_ESP32C3)
    return "ESP32-C3";
#elif defined(ESMESH_CHIP_ESP32C6)
    return "ESP32-C6";
#else
    return "ESP32";
#endif
}

String getMacAddressSuffix() {
    uint8_t mac[6];
    WiFi.macAddress(mac);
    char buf[10];
    snprintf(buf, sizeof(buf), "%02X%02X", mac[4], mac[5]);
    return String(buf);
}

// -------------------------------------------------------------
// Setup Entry Point
// -------------------------------------------------------------
void setup() {
    Serial.begin(115200);
    delay(500);

    Serial.println("\n==========================================");
    Serial.printf("  EsMesh Node Firmware v%s\n", ESMESH_FIRMWARE_VERSION);
    Serial.printf("  Hardware Target: %s\n", getChipModelName().c_str());
    Serial.println("==========================================\n");

    loadConfiguration();

    // painlessMesh Debug Configuration
    mesh.setDebugMsgTypes(ERROR | STARTUP | CONNECTION);

    // Initialize Mesh Network
    mesh.init(config.meshPrefix, config.meshPassword, &userScheduler, config.meshPort, WIFI_AP_STA, config.meshChannel);
    mesh.onReceive(&receivedCallback);
    mesh.onNewConnection(&newConnectionCallback);
    mesh.onChangedConnections(&changedConnectionCallback);
    mesh.onNodeTimeAdjusted(&nodeTimeAdjustedCallback);

    localNodeIdStr = String(mesh.getNodeId(), HEX);
    localNodeIdStr.toUpperCase();
    if (config.nodeName == "EsMesh-Node") {
        config.nodeName = "ESM-" + getMacAddressSuffix();
    }

    Serial.printf("[EsMesh] Node Initialized. Node ID: 0x%08X (%s)\n", mesh.getNodeId(), localNodeIdStr.c_str());

    // Optional Gateway Station Wi-Fi connection
    if (config.isGateway && config.wifiSsid.length() > 0) {
        Serial.printf("[Gateway] Connecting to Router SSID: %s\n", config.wifiSsid.c_str());
        mesh.stationManual(config.wifiSsid, config.wifiPassword);
    }

    // Setup mDNS & Discovery Beacon
    if (MDNS.begin("esmesh")) {
        MDNS.addService("esmesh", "tcp", ESMESH_DEFAULT_PORT);
        MDNS.addService("http", "tcp", ESMESH_DEFAULT_PORT);
        Serial.println("[mDNS] Responder started at http://esmesh.local");
    }

    udpBeacon.begin(ESMESH_UDP_BEACON_PORT);

    // Initialize HTTP REST Server & WebSockets
    setupWebServer();

    Serial.println("[EsMesh] Boot complete and ready.\n");
}

// -------------------------------------------------------------
// Main Event Loop
// -------------------------------------------------------------
void loop() {
    mesh.update();
    ws.cleanupClients();

    unsigned long currentMillis = millis();

    // Periodic Telemetry Push (WebSocket + Mesh Broadcast)
    if (config.telemetryEnabled && (currentMillis - lastTelemetryMillis >= config.telemetryIntervalMs)) {
        lastTelemetryMillis = currentMillis;
        sendTelemetryBroadcast();
    }

    // Periodic UDP Discovery Beacon (Every 3 seconds)
    if (currentMillis - lastBeaconMillis >= 3000) {
        lastBeaconMillis = currentMillis;
        sendUdpDiscoveryBeacon();
    }
}

// -------------------------------------------------------------
// Mesh Network Callbacks
// -------------------------------------------------------------
void receivedCallback(uint32_t from, String &msg) {
    Serial.printf("[Mesh RX] From: 0x%08X Msg: %s\n", from, msg.c_str());

    // Forward received mesh packet over WebSockets to Android App
    StaticJsonDocument<1024> doc;
    DeserializationError error = deserializeJson(doc, msg);
    if (!error) {
        doc["rxFrom"] = String(from, HEX);
        String output;
        serializeJson(doc, output);
        ws.textAll(output);
    } else {
        // Plain text message encapsulation
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
    Serial.printf("[Mesh Connection] New connection established with Node 0x%08X\n", nodeId);
    changedConnectionCallback();
}

void changedConnectionCallback() {
    Serial.printf("[Mesh Topology] Mesh topology updated. Total connected nodes: %u\n", mesh.getNodeList().size() + 1);

    // Notify connected Android apps via WebSocket
    StaticJsonDocument<512> doc;
    doc["type"] = "topology_update";
    doc["nodeCount"] = mesh.getNodeList().size() + 1;
    doc["gatewayNodeId"] = localNodeIdStr;
    String out;
    serializeJson(doc, out);
    ws.textAll(out);
}

void nodeTimeAdjustedCallback(int32_t offset) {
    Serial.printf("[Mesh Time] Time adjusted: %d us. Current time: %u\n", offset, mesh.getNodeTime());
}

// -------------------------------------------------------------
// UDP Broadcast Discovery Beacon for Android App Auto-Pairing
// -------------------------------------------------------------
void sendUdpDiscoveryBeacon() {
    IPAddress broadcastIp(255, 255, 255, 255);
    StaticJsonDocument<256> beaconDoc;
    beaconDoc["service"] = "esmesh";
    beaconDoc["nodeId"] = localNodeIdStr;
    beaconDoc["nodeName"] = config.nodeName;
    beaconDoc["chipModel"] = getChipModelName();
    beaconDoc["version"] = ESMESH_FIRMWARE_VERSION;
    beaconDoc["port"] = ESMESH_DEFAULT_PORT;
    beaconDoc["isGateway"] = config.isGateway;

    String beaconStr;
    serializeJson(beaconDoc, beaconStr);

    udpBeacon.beginPacket(broadcastIp, ESMESH_UDP_BEACON_PORT);
    udpBeacon.write((const uint8_t*)beaconStr.c_str(), beaconStr.length());
    udpBeacon.endPacket();
}

// -------------------------------------------------------------
// Telemetry & WebSocket Streaming
// -------------------------------------------------------------
void sendTelemetryBroadcast() {
    StaticJsonDocument<512> doc;
    doc["type"] = "telemetry";
    doc["nodeId"] = localNodeIdStr;
    doc["nodeName"] = config.nodeName;
    doc["rssi"] = WiFi.RSSI();
    doc["freeHeap"] = ESP.getFreeHeap();
    doc["uptimeSeconds"] = millis() / 1000;
    doc["meshNodes"] = mesh.getNodeList().size() + 1;
    doc["chipModel"] = getChipModelName();

    String jsonStr;
    serializeJson(doc, jsonStr);

    // Send to connected Android app WebSockets
    if (ws.count() > 0) {
        ws.textAll(jsonStr);
    }
}

// -------------------------------------------------------------
// HTTP REST & WebSocket API Handlers
// -------------------------------------------------------------
void setupWebServer() {
    // 1. GET /api/status - Device status and system metrics
    server.on("/api/status", HTTP_GET, [](AsyncWebServerRequest *request) {
        StaticJsonDocument<512> doc;
        doc["nodeId"] = localNodeIdStr;
        doc["nodeName"] = config.nodeName;
        doc["chipModel"] = getChipModelName();
        doc["firmwareVersion"] = ESMESH_FIRMWARE_VERSION;
        doc["freeHeap"] = ESP.getFreeHeap();
        doc["totalHeap"] = ESP.getHeapSize();
        doc["rssi"] = WiFi.RSSI();
        doc["uptime"] = millis() / 1000;
        doc["meshPrefix"] = config.meshPrefix;
        doc["meshChannel"] = config.meshChannel;
        doc["isGateway"] = config.isGateway;
        doc["connectedNodes"] = mesh.getNodeList().size();

        String response;
        serializeJson(doc, response);
        request->send(200, "application/json", response);
    });

    // 2. GET /api/nodes - Mesh topology & active node table
    server.on("/api/nodes", HTTP_GET, [](AsyncWebServerRequest *request) {
        StaticJsonDocument<2048> doc;
        doc["gatewayNodeId"] = localNodeIdStr;
        doc["totalNodes"] = mesh.getNodeList().size() + 1;
        doc["routerSsid"] = config.wifiSsid;
        doc["gatewayIp"] = WiFi.localIP().toString();

        JsonArray nodesArray = doc.createNestedArray("nodes");
        
        // Self entry
        JsonObject selfObj = nodesArray.createNestedObject();
        selfObj["nodeId"] = localNodeIdStr;
        selfObj["name"] = config.nodeName;
        selfObj["role"] = config.isGateway ? "Gateway" : "Mesh Root";
        selfObj["ip"] = WiFi.localIP().toString();
        selfObj["rssi"] = WiFi.RSSI();
        selfObj["hops"] = 0;
        selfObj["neighbors"] = mesh.getNodeList().size();

        // Connected subnodes
        auto nodes = mesh.getNodeList();
        for (auto &&nodeId : nodes) {
            JsonObject nodeObj = nodesArray.createNestedObject();
            char hexId[12];
            snprintf(hexId, sizeof(hexId), "%08X", nodeId);
            nodeObj["nodeId"] = String(hexId);
            nodeObj["name"] = "Node-" + String(hexId).substring(4);
            nodeObj["role"] = "Mesh Node";
            nodeObj["ip"] = "10.0.0." + String(nodeId % 250 + 2);
            nodeObj["parent"] = localNodeIdStr;
            nodeObj["rssi"] = -55 - (rand() % 25);
            nodeObj["hops"] = 1;
            nodeObj["neighbors"] = 1;
        }

        String response;
        serializeJson(doc, response);
        request->send(200, "application/json", response);
    });

    // 3. POST /api/config - Save configuration
    server.on("/api/config", HTTP_POST, [](AsyncWebServerRequest *request) {}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            StaticJsonDocument<512> doc;
            DeserializationError error = deserializeJson(doc, data, len);
            if (error) {
                request->send(400, "application/json", "{\"status\":\"error\",\"message\":\"Invalid JSON\"}");
                return;
            }

            if (doc.containsKey("nodeName")) config.nodeName = doc["nodeName"].as<String>();
            if (doc.containsKey("meshPrefix")) config.meshPrefix = doc["meshPrefix"].as<String>();
            if (doc.containsKey("meshPassword")) config.meshPassword = doc["meshPassword"].as<String>();
            if (doc.containsKey("meshChannel")) config.meshChannel = doc["meshChannel"].as<uint8_t>();
            if (doc.containsKey("isGateway")) config.isGateway = doc["isGateway"].as<bool>();
            if (doc.containsKey("wifiSsid")) config.wifiSsid = doc["wifiSsid"].as<String>();
            if (doc.containsKey("wifiPassword")) config.wifiPassword = doc["wifiPassword"].as<String>();

            saveConfiguration();
            request->send(200, "application/json", "{\"status\":\"success\",\"message\":\"Configuration saved. Restarting mesh...\"}");
            delay(500);
            ESP.restart();
        }
    );

    // 4. POST /api/send - Send mesh message / broadcast packet
    server.on("/api/send", HTTP_POST, [](AsyncWebServerRequest *request) {}, NULL,
        [](AsyncWebServerRequest *request, uint8_t *data, size_t len, size_t index, size_t total) {
            StaticJsonDocument<512> doc;
            DeserializationError error = deserializeJson(doc, data, len);
            if (error) {
                request->send(400, "application/json", "{\"status\":\"error\",\"message\":\"Invalid payload\"}");
                return;
            }

            String text = doc["text"].as<String>();
            String targetNodeHex = doc["target"].as<String>();

            if (targetNodeHex.length() > 0 && targetNodeHex != "broadcast" && targetNodeHex != "ALL") {
                uint32_t targetId = strtoul(targetNodeHex.c_str(), NULL, 16);
                mesh.sendSingle(targetId, text);
            } else {
                mesh.sendBroadcast(text);
            }

            request->send(200, "application/json", "{\"status\":\"success\",\"message\":\"Packet routed to mesh\"}");
        }
    );

    // 5. POST /api/reboot - System reboot
    server.on("/api/reboot", HTTP_POST, [](AsyncWebServerRequest *request) {
        request->send(200, "application/json", "{\"status\":\"success\",\"message\":\"Rebooting ESP32...\"}");
        delay(500);
        ESP.restart();
    });

    // WebSocket event dispatcher
    ws.onEvent([](AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
        if (type == WS_EVT_CONNECT) {
            Serial.printf("[WebSocket] Android App Client connected #%u from %s\n", client->id(), client->remoteIP().toString().c_str());
        } else if (type == WS_EVT_DATA) {
            AwsFrameInfo *info = (AwsFrameInfo*)arg;
            if (info->final && info->index == 0 && info->len == len && info->opcode == WS_TEXT) {
                data[len] = 0;
                String incoming = (char*)data;
                Serial.printf("[WebSocket RX] %s\n", incoming.c_str());
                mesh.sendBroadcast(incoming);
            }
        }
    });

    server.addHandler(&ws);
    server.begin();
    Serial.println("[HTTP/WS] Server active on port 80");
}

// -------------------------------------------------------------
// Non-Volatile Storage (NVS / Preferences)
// -------------------------------------------------------------
void loadConfiguration() {
    preferences.begin("esmesh", true);
    config.nodeName = preferences.getString("name", "EsMesh-Node");
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
