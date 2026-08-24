# EsMesh WebSocket Streaming Protocol

WebSocket Endpoint: `ws://<DEVICE_IP>:<WS_PORT>/api/v1/ws`

## 1. Connection Lifecycle
1. Client connects via `ws://192.168.1.120/api/v1/ws`.
2. Gateway accepts connection and registers the client as an active streaming endpoint.
3. Every 15 seconds, ping/pong frames are exchanged to keep the TCP channel alive.
4. If connection drops, the Android client utilizes an exponential backoff reconnect strategy (2s, 4s, 6s... up to 10s).

## 2. Inbound Message Stream
When any mesh node emits a packet, the gateway transmits the JSON text frame to all connected WebSocket subscribers:

```json
{
  "protocol": "EsMesh/1",
  "type": "message",
  "id": "MSG-77A1",
  "source": "ESM-C3D4",
  "destination": "BROADCAST",
  "timestamp": 1719234900000,
  "ttl": 4,
  "payload": "Sensor reading: Temp=24.5C"
}
```

## 3. Outbound Message Dispatch
Client sends the JSON string frame directly into the socket. The gateway reads the destination:
- If `BROADCAST`: Broadcasts to RF mesh peers and other WebSocket sessions.
- If specific Node ID: Looks up next-hop in routing table and transmits packet.
