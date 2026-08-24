# EsMesh Device Discovery

## 1. mDNS / DNS-SD Service Discovery
ESP32 Gateway devices advertise themselves over local network multicast with:
- Service Type: `_espmesh._tcp.`
- Port: 80 (or custom configured HTTP port)
- TXT Records: `model=ESP32-S3`, `node_id=ESM-A1B2`, `proto=EsMesh/1`

Android resolves discovered services using `android.net.nsd.NsdManager` with `WifiManager.MulticastLock` enabled.

---

## 2. UDP Broadcast Beacon
- Port: `8266` (configurable)
- Payload: `{"protocol":"EsMesh/1","type":"discover"}`
- Gateway responds with its node identity and status packet directly to the sender's IP address.

---

## 3. Subnet Sweep Fallback
For network routers that filter multicast/broadcast packets, the client supports direct probe sweeps across standard gateway addresses (e.g. `192.168.1.120`, `192.168.4.1`).
