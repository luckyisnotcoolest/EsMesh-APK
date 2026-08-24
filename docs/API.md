# EsMesh REST API Specification

Base URL: `http://<DEVICE_IP>:<HTTP_PORT>` (Default: `http://192.168.1.120:80`)

---

### `GET /api/v1/status`
Returns live system health, uptime, and mesh telemetry.

#### Response (200 OK):
```json
{
  "name": "ESP32 Gateway",
  "model": "ESP32-S3",
  "node_id": "ESM-A1B2",
  "rssi": -48,
  "uptime": 12450,
  "free_heap": 210400,
  "mesh_nodes": 4,
  "packets_relayed": 1390
}
```

---

### `GET /api/v1/capabilities`
Returns list of features supported by the node.

#### Response (200 OK):
```json
{
  "capabilities": ["wifi_sta", "wifi_ap", "websocket", "http", "mdns", "mesh", "repeater"]
}
```

---

### `GET /api/v1/device`
Returns full hardware specifications and network addresses.

---

### `GET /api/v1/network`
Returns list of connected mesh nodes and active routing table.

#### Response (200 OK):
```json
{
  "gateway_node_id": "ESM-A1B2",
  "gateway_ip": "192.168.1.120",
  "router_ssid": "MyRouter",
  "total_nodes": 4,
  "active_routes": 3,
  "nodes": [
    {
      "node_id": "ESM-A1B2",
      "name": "ESP32 Gateway",
      "role": "Root Gateway",
      "ip": "192.168.1.120",
      "rssi": -48,
      "hops": 0
    },
    {
      "node_id": "ESM-C3D4",
      "name": "Garden Relay",
      "role": "Mesh Node",
      "ip": "10.0.0.2",
      "rssi": -65,
      "hops": 1
    }
  ],
  "routes": [
    {
      "destination": "ESM-C3D4",
      "next_hop": "ESM-C3D4",
      "hops": 1,
      "rssi": -65,
      "state": "Active"
    }
  ]
}
```

---

### `POST /api/v1/message`
Transmits a mesh packet via REST fallback.
Body: JSON formatted `EsMesh/1` packet.

---

### `POST /api/v1/config`
Updates device configuration (SSID, AP settings, ports, mesh topology).

---

### `POST /api/v1/restart`
Reboots the ESP32 node.
