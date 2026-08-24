package com.example

import android.app.Application
import com.example.data.discovery.EsMeshDiscoveryService
import com.example.data.flasher.FlashBackend
import com.example.data.flasher.UsbSerialFlasher
import com.example.data.local.AppDatabase
import com.example.data.network.EsMeshHttpClient
import com.example.data.network.EsMeshWebSocketClient
import com.example.data.repository.ConnectionRepository
import com.example.data.repository.DeviceRepository
import com.example.data.repository.DiagnosticsRepository
import com.example.data.repository.MessageRepository
import com.example.data.repository.SettingsRepository

class EsMeshApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var httpClient: EsMeshHttpClient
        private set

    lateinit var webSocketClient: EsMeshWebSocketClient
        private set

    lateinit var deviceRepository: DeviceRepository
        private set

    lateinit var messageRepository: MessageRepository
        private set

    lateinit var connectionRepository: ConnectionRepository
        private set

    lateinit var diagnosticsRepository: DiagnosticsRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var discoveryService: EsMeshDiscoveryService
        private set

    lateinit var flashBackend: FlashBackend
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getDatabase(this)
        httpClient = EsMeshHttpClient()
        webSocketClient = EsMeshWebSocketClient()

        deviceRepository = DeviceRepository(database.deviceDao())
        messageRepository = MessageRepository(database.messageDao())
        connectionRepository = ConnectionRepository(
            httpClient = httpClient,
            webSocketClient = webSocketClient,
            deviceRepository = deviceRepository,
            messageRepository = messageRepository
        )
        diagnosticsRepository = DiagnosticsRepository(database.diagnosticDao(), httpClient)
        settingsRepository = SettingsRepository(database.settingDao())

        discoveryService = EsMeshDiscoveryService(this, httpClient)
        flashBackend = UsbSerialFlasher()
    }
}
