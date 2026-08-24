package com.example.data.network

import com.example.data.model.EsMeshConfig
import com.example.data.model.EsMeshDevice
import com.example.data.model.EsMeshMessage
import com.example.data.model.EsMeshNetworkTopology
import com.example.data.model.MeshNodeItem
import com.example.data.model.NetworkRouteItem
import com.example.data.protocol.EsMeshProtocolValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class NetworkResponse<out T> {
    data class Success<out T>(val data: T) : NetworkResponse<T>()
    data class Error(val code: Int, val message: String) : NetworkResponse<Nothing>()
    data class Failure(val exception: Throwable) : NetworkResponse<Nothing>()
}

class EsMeshHttpClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun baseUrl(ip: String, port: Int = 80): String {
        return if (port == 80) "http://$ip" else "http://$ip:$port"
    }

    suspend fun getStatus(ip: String, port: Int = 80): NetworkResponse<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/status")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = if (body.isNotBlank()) JSONObject(body) else JSONObject()
                    NetworkResponse.Success(json)
                } else {
                    NetworkResponse.Error(response.code, "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun getCapabilities(ip: String, port: Int = 80): NetworkResponse<List<String>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/capabilities")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val array = json.optJSONArray("capabilities") ?: JSONArray()
                    val list = mutableListOf<String>()
                    for (i in 0 until array.length()) {
                        list.add(array.getString(i))
                    }
                    NetworkResponse.Success(list)
                } else {
                    NetworkResponse.Error(response.code, "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun getDevice(ip: String, port: Int = 80): NetworkResponse<EsMeshDevice> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/device")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val capsArray = json.optJSONArray("capabilities") ?: JSONArray()
                    val caps = mutableListOf<String>()
                    for (i in 0 until capsArray.length()) {
                        caps.add(capsArray.getString(i))
                    }

                    val device = EsMeshDevice(
                        id = json.optString("id", "ESM-${ip.takeLast(4)}"),
                        name = json.optString("name", "ESP32 Gateway"),
                        model = json.optString("model", "ESP32-S3"),
                        chip = json.optString("chip", "ESP32-S3-WROOM-1"),
                        nodeId = json.optString("node_id", "ESM-A1B2"),
                        ipAddress = ip,
                        httpPort = port,
                        wsPort = json.optInt("ws_port", 80),
                        macAddress = json.optString("mac", "84:F7:03:A1:B2:C3"),
                        rssi = json.optInt("rssi", -48),
                        firmwareVersion = json.optString("firmware", "v1.4.2"),
                        protocolVersion = json.optString("protocol", "EsMesh/1"),
                        capabilities = if (caps.isNotEmpty()) caps else listOf("wifi_sta", "wifi_ap", "websocket", "http", "mdns", "mesh"),
                        wifiMode = json.optString("wifi_mode", "STA+AP"),
                        wifiBand = json.optString("wifi_band", "2.4 GHz"),
                        ssid = json.optString("ssid", "MyRouter"),
                        uptimeSeconds = json.optLong("uptime", 12345L),
                        lastSeen = System.currentTimeMillis()
                    )
                    NetworkResponse.Success(device)
                } else {
                    NetworkResponse.Error(response.code, "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun getNetworkTopology(ip: String, port: Int = 80): NetworkResponse<EsMeshNetworkTopology> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/network")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val nodesArray = json.optJSONArray("nodes") ?: JSONArray()
                    val nodes = mutableListOf<MeshNodeItem>()
                    for (i in 0 until nodesArray.length()) {
                        val nodeObj = nodesArray.getJSONObject(i)
                        nodes.add(
                            MeshNodeItem(
                                nodeId = nodeObj.optString("node_id"),
                                name = nodeObj.optString("name", "Node"),
                                role = nodeObj.optString("role", "Mesh Node"),
                                ipAddress = nodeObj.optString("ip", "10.0.0.$i"),
                                parentNodeId = if (nodeObj.has("parent")) nodeObj.optString("parent") else null,
                                rssi = nodeObj.optInt("rssi", -60),
                                hops = nodeObj.optInt("hops", 1),
                                lastSeenSecAgo = nodeObj.optInt("last_seen", 2),
                                isReachable = nodeObj.optBoolean("reachable", true)
                            )
                        )
                    }

                    val routesArray = json.optJSONArray("routes") ?: JSONArray()
                    val routes = mutableListOf<NetworkRouteItem>()
                    for (i in 0 until routesArray.length()) {
                        val rObj = routesArray.getJSONObject(i)
                        routes.add(
                            NetworkRouteItem(
                                destinationNode = rObj.optString("destination"),
                                nextHopNode = rObj.optString("next_hop"),
                                metricHops = rObj.optInt("hops", 1),
                                signalStrength = rObj.optInt("rssi", -60),
                                state = rObj.optString("state", "Active")
                            )
                        )
                    }

                    val topology = EsMeshNetworkTopology(
                        gatewayNodeId = json.optString("gateway_node_id", "ESM-A1B2"),
                        routerSsid = json.optString("router_ssid", "MyRouter"),
                        gatewayIp = ip,
                        totalNodes = json.optInt("total_nodes", nodes.size),
                        activeRoutes = json.optInt("active_routes", routes.size),
                        nodes = nodes,
                        routes = routes
                    )
                    NetworkResponse.Success(topology)
                } else {
                    NetworkResponse.Error(response.code, "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun getConfig(ip: String, port: Int = 80): NetworkResponse<EsMeshConfig> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/config")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val config = EsMeshConfig(
                        deviceName = json.optString("device_name", "ESP32 Gateway"),
                        nodeId = json.optString("node_id", "ESM-A1B2"),
                        operatingMode = json.optString("operating_mode", "Gateway"),
                        wifiSsid = json.optString("wifi_ssid", "MyRouter"),
                        isDhcp = json.optBoolean("is_dhcp", true),
                        staticIp = json.optString("static_ip", ip),
                        gateway = json.optString("gateway", "192.168.1.1"),
                        subnet = json.optString("subnet", "255.255.255.0"),
                        dns = json.optString("dns", "8.8.8.8"),
                        apEnabled = json.optBoolean("ap_enabled", true),
                        apSsid = json.optString("ap_ssid", "EsMesh-Gateway-A1B2"),
                        apChannel = json.optInt("ap_channel", 6),
                        maxClients = json.optInt("max_clients", 4),
                        httpPort = json.optInt("http_port", port),
                        wsPort = json.optInt("ws_port", 80),
                        mdnsName = json.optString("mdns_name", "espmesh"),
                        discoveryIntervalSec = json.optInt("discovery_interval", 10),
                        timeoutSec = json.optInt("timeout", 5),
                        retryCount = json.optInt("retry_count", 3),
                        meshEnabled = json.optBoolean("mesh_enabled", true),
                        maxHops = json.optInt("max_hops", 8),
                        ttl = json.optInt("ttl", 5),
                        repeaterMode = json.optBoolean("repeater_mode", false),
                        apiAuthEnabled = json.optBoolean("api_auth_enabled", false),
                        encryptionEnabled = json.optBoolean("encryption_enabled", false)
                    )
                    NetworkResponse.Success(config)
                } else {
                    NetworkResponse.Error(response.code, "HTTP ${response.code}: $body")
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun postConfig(ip: String, port: Int = 80, config: EsMeshConfig): NetworkResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("device_name", config.deviceName)
                put("node_id", config.nodeId)
                put("operating_mode", config.operatingMode)
                put("wifi_ssid", config.wifiSsid)
                if (config.wifiPassword.isNotBlank()) put("wifi_password", config.wifiPassword)
                put("is_dhcp", config.isDhcp)
                put("static_ip", config.staticIp)
                put("gateway", config.gateway)
                put("subnet", config.subnet)
                put("dns", config.dns)
                put("ap_enabled", config.apEnabled)
                put("ap_ssid", config.apSsid)
                if (config.apPassword.isNotBlank()) put("ap_password", config.apPassword)
                put("ap_channel", config.apChannel)
                put("max_clients", config.maxClients)
                put("http_port", config.httpPort)
                put("ws_port", config.wsPort)
                put("mdns_name", config.mdnsName)
                put("discovery_interval", config.discoveryIntervalSec)
                put("timeout", config.timeoutSec)
                put("retry_count", config.retryCount)
                put("mesh_enabled", config.meshEnabled)
                put("max_hops", config.maxHops)
                put("ttl", config.ttl)
                put("repeater_mode", config.repeaterMode)
                put("api_auth_enabled", config.apiAuthEnabled)
                if (config.authToken.isNotBlank()) put("auth_token", config.authToken)
                put("encryption_enabled", config.encryptionEnabled)
                if (config.encryptionKey.isNotBlank()) put("encryption_key", config.encryptionKey)
            }

            val body = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/config")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    NetworkResponse.Success(true)
                } else {
                    NetworkResponse.Error(response.code, response.body?.string().orEmpty())
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun postMessage(ip: String, port: Int = 80, message: EsMeshMessage): NetworkResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val jsonString = EsMeshProtocolValidator.serializeMessage(message)
            val body = jsonString.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/message")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    NetworkResponse.Success(true)
                } else {
                    NetworkResponse.Error(response.code, response.body?.string().orEmpty())
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun restartDevice(ip: String, port: Int = 80): NetworkResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/restart")
                .post("{}".toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    NetworkResponse.Success(true)
                } else {
                    NetworkResponse.Error(response.code, response.body?.string().orEmpty())
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }

    suspend fun testWifi(ip: String, port: Int = 80, ssid: String, pass: String): NetworkResponse<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("ssid", ssid)
                put("password", pass)
            }
            val request = Request.Builder()
                .url("${baseUrl(ip, port)}/api/v1/wifi/test")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    NetworkResponse.Success(true)
                } else {
                    NetworkResponse.Error(response.code, response.body?.string().orEmpty())
                }
            }
        } catch (e: Exception) {
            NetworkResponse.Failure(e)
        }
    }
}
