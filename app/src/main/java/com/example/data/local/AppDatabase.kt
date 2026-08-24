package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.DiagnosticDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.dao.SettingDao
import com.example.data.local.entity.DeviceEntity
import com.example.data.local.entity.DiagnosticLogEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.SettingEntity

@Database(
    entities = [
        DeviceEntity::class,
        MessageEntity::class,
        DiagnosticLogEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun messageDao(): MessageDao
    abstract fun diagnosticDao(): DiagnosticDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esmesh_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
