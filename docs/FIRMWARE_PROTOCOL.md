# EsMesh Firmware & Protocol Specification (EsMesh/1)

## 1. Overview
The **EsMesh/1** protocol defines a unified JSON-based telemetry and packet routing standard for ESP32 mesh nodes, gateways, and Android clients.

## 2. Core Packet Structure
All messages transmitted across WebSocket, UDP, or HTTP MUST comply with this JSON schema:

```json
{
  "protocol": "EsMesh/1",
  "type": "message",
  "id": "MSG-A1B2",
  "source": "ESM-A1B2",
  "destination": "BROADCAST",
  "timestamp": 1719234859000,
  "ttl": 5,
  "payload": "Node status nominal"
}
```

### Required Fields
| Field | Type | Description |
| :--- | :--- | :--- |
| `protocol` | String | Must be `"EsMesh/1"`. Packets with mismatching versions will be dropped. |
| `type` | String | Message category: `message`, `status`, `ping`, `pong`, `node_join`, `node_leave`, `route_update`, `ack`. |
| `id` | String | Unique packet tracking identifier (e.g. `MSG-4F9A`). |
| `source` | String | Origin node identifier (e.g. `ESM-A1B2` or `PHONE`). |
| `destination` | String | Target node ID or `BROADCAST` to flood all active peers. |
| `timestamp` | Long | Unix epoch timestamp in milliseconds. |
| `ttl` | Integer | Time-To-Live hop counter. Decremented by 1 at each intermediate hop. Drop if `ttl <= 0`. Maximum is 15. |
| `payload` | String | UTF-8 data payload (max 4096 bytes). |

---

## 3. Node Roles
1. **Gateway Node**: Connected to client via AP or local Station Wi-Fi; runs HTTP REST + WebSocket server on port 80.
2. **Repeater Node**: Relays packets across mesh RF channels while decrementing TTL.
3. **Sensor/Endpoint Node**: Periodic packet emitter.
