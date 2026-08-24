package com.example.data.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.example.data.model.EsMeshDevice
import com.example.data.network.EsMeshHttpClient
import com.example.data.network.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

data class DiscoveryState(
    val isScanning: Boolean = false,
    val discoveredDevices: List<EsMeshDevice> = emptyList(),
    val scanMessage: String = "Idle"
)

class EsMeshDiscoveryService(
    private val context: Context,
    private val httpClient: EsMeshHttpClient
) {
    private val _discoveryState = MutableStateFlow(DiscoveryState())
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    private var nsdManager: NsdManager? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var multicastLock: WifiManager.MulticastLock? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var scanJob: Job? = null

    private val discoveredMap = mutableMapOf<String, EsMeshDevice>()

    companion object {
        const val SERVICE_TYPE = "_espmesh._tcp."
        const val UDP_DISCOVERY_PORT = 8266
    }

    fun startDiscovery() {
        if (_discoveryState.value.isScanning) return
        discoveredMap.clear()
        _discoveryState.value = DiscoveryState(
            isScanning = true,
            discoveredDevices = emptyList(),
            scanMessage = "Acquiring multicast lock and scanning mDNS / UDP..."
        )

        acquireMulticastLock()
        startMdnsDiscovery()
        startUdpBroadcastDiscovery()
        startSubnetSweepDiscovery()

        // Auto-stop full active sweep after 15 seconds
        scanJob?.cancel()
        scanJob = scope.launch {
            delay(15000L)
            if (_discoveryState.value.isScanning) {
                stopDiscovery()
            }
        }
    }

    private fun acquireMulticastLock() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("EsMeshMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
        } catch (e: Exception) {
            Log.e("EsMeshDiscovery", "Failed to acquire multicast lock: ${e.message}")
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
            multicastLock = null
        } catch (_: Exception) {}
    }

    private fun startMdnsDiscovery() {
        try {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e("EsMeshDiscovery", "mDNS start failed: $errorCode")
                }

                override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                    Log.e("EsMeshDiscovery", "mDNS stop failed: $errorCode")
                }

                override fun onDiscoveryStarted(serviceType: String?) {
                    Log.d("EsMeshDiscovery", "mDNS discovery started for $serviceType")
                }

                override fun onDiscoveryStopped(serviceType: String?) {
                    Log.d("EsMeshDiscovery", "mDNS discovery stopped")
                }

                override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
                    if (serviceInfo == null) return
                    resolveMdnsService(serviceInfo)
                }

                override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
                    Log.d("EsMeshDiscovery", "Service lost: ${serviceInfo?.serviceName}")
                }
            }

            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e("EsMeshDiscovery", "mDNS discovery error: ${e.message}")
        }
    }

    private fun resolveMdnsService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.w("EsMeshDiscovery", "Resolve failed for ${serviceInfo?.serviceName}: $errorCode")
            }

            override fun onServiceResolved(resolvedInfo: NsdServiceInfo?) {
                if (resolvedInfo == null) return
                val host = resolvedInfo.host?.hostAddress ?: return
                val port = resolvedInfo.port
                scope.launch {
                    probeAndAddDevice(host, port)
                }
            }
        })
    }

    private fun startUdpBroadcastDiscovery() {
        scope.launch {
            try {
                val socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    broadcast = true
                    bind(InetSocketAddress(UDP_DISCOVERY_PORT))
                    soTimeout = 4000
                }

                // Send discovery ping packet
                val discoveryPacket = "{\"protocol\":\"EsMesh/1\",\"type\":\"discover\"}".toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket(discoveryPacket, discoveryPacket.size, broadcastAddress, UDP_DISCOVERY_PORT)
                socket.send(packet)

                val buffer = ByteArray(1024)
                val responsePacket = DatagramPacket(buffer, buffer.size)

                while (isActive && _discoveryState.value.isScanning) {
                    try {
                        socket.receive(responsePacket)
                        val sourceIp = responsePacket.address.hostAddress ?: continue
                        probeAndAddDevice(sourceIp, 80)
                    } catch (_: java.net.SocketTimeoutException) {
                        // Loop to keep checking or resend
                        if (isActive && _discoveryState.value.isScanning) {
                            try {
                                socket.send(packet)
                            } catch (_: Exception) {}
                        }
                    }
                }
                socket.close()
            } catch (e: Exception) {
                Log.d("EsMeshDiscovery", "UDP discovery note: ${e.message}")
            }
        }
    }

    private fun startSubnetSweepDiscovery() {
        scope.launch {
            // Proactively probe common and typical local gateway IPs
            val commonIps = listOf("192.168.1.120", "192.168.1.100", "192.168.4.1", "192.168.0.120", "10.0.0.120")
            for (ip in commonIps) {
                if (!isActive || !_discoveryState.value.isScanning) break
                probeAndAddDevice(ip, 80)
            }
        }
    }

    private suspend fun probeAndAddDevice(ip: String, port: Int) = withContext(Dispatchers.IO) {
        if (discoveredMap.containsKey(ip)) return@withContext

        when (val res = httpClient.getDevice(ip, port)) {
            is NetworkResponse.Success -> {
                val dev = res.data
                discoveredMap[ip] = dev
                _discoveryState.value = _discoveryState.value.copy(
                    discoveredDevices = discoveredMap.values.toList(),
                    scanMessage = "Found ${discoveredMap.size} EsMesh node(s)"
                )
            }
            else -> {
                // If direct device endpoint not full, check status
                when (val statusRes = httpClient.getStatus(ip, port)) {
                    is NetworkResponse.Success -> {
                        val json = statusRes.data
                        val dev = EsMeshDevice(
                            id = json.optString("node_id", "ESM-${ip.takeLast(4)}"),
                            name = json.optString("name", "ESP32 Mesh Node"),
                            model = json.optString("model", "ESP32-S3"),
                            nodeId = json.optString("node_id", "ESM-${ip.takeLast(4)}"),
                            ipAddress = ip,
                            httpPort = port,
                            wsPort = port
                        )
                        discoveredMap[ip] = dev
                        _discoveryState.value = _discoveryState.value.copy(
                            discoveredDevices = discoveredMap.values.toList(),
                            scanMessage = "Found ${discoveredMap.size} EsMesh node(s)"
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    fun stopDiscovery() {
        try {
            discoveryListener?.let { nsdManager?.stopServiceDiscovery(it) }
        } catch (_: Exception) {}
        discoveryListener = null
        releaseMulticastLock()
        scanJob?.cancel()
        _discoveryState.value = _discoveryState.value.copy(
            isScanning = false,
            scanMessage = "Discovery completed. Found ${discoveredMap.size} node(s)."
        )
    }
}
