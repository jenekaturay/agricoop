package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.AgriCoopDao
import com.example.data.db.dao.AuditLogDao
import com.example.data.db.entities.AuditLogEntity
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.security.DatabaseEncryptionManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        CooperativeEntity::class,
        FarmerEntity::class,
        ProduceBatchEntity::class,
        HubOperationEntity::class,
        MoMoFloatEntity::class,
        AuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun agriCoopDao(): AgriCoopDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = DatabaseEncryptionManager.getOrCreatePassphrase(context.applicationContext)
                val factory = SupportOpenHelperFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agricoop_liberia.db"
                )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }

        @Synchronized
        fun closeAndResetInstance() {
            INSTANCE?.let { db ->
                if (db.isOpen) {
                    db.close()
                }
            }
            INSTANCE = null
        }
    }
}
