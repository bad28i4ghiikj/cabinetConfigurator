package com.example.cabinetconfigurator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ParameterDefinitionEntity::class,
        PricingProfileEntity::class,
        PricingParameterValueEntity::class,
        QuoteEntity::class,
        QuoteZoneEntity::class,
        QuotePricingSnapshotEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pricingDao(): PricingDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cabinet_configurator.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
