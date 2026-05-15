package com.example.cabinetconfigurator.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "parameter_definitions")
data class ParameterDefinitionEntity(
    @PrimaryKey val key: String,
    val label: String,
    val category: String,
    val valueType: String,
    val unit: String?,
    val sortOrder: Int
)

@Entity(tableName = "pricing_profiles")
data class PricingProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val isActive: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pricing_parameter_values", primaryKeys = ["profileId", "parameterKey"])
data class PricingParameterValueEntity(
    val profileId: Long,
    val parameterKey: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val cabinetType: String,
    val elementType: String,
    val widthMm: Int,
    val heightMm: Int,
    val depthMm: Int,
    val totalNet: Double,
    val totalGross: Double,
    val pricingProfileId: Long?,
    val pricingProfileName: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quote_zones",
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quoteId")]
)
data class QuoteZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val quoteId: Long,
    val name: String,
    val orderIndex: Int,
    val quantity: Int
)

@Entity(
    tableName = "quote_pricing_snapshot",
    primaryKeys = ["quoteId", "parameterKey"],
    foreignKeys = [
        ForeignKey(
            entity = QuoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quoteId")]
)
data class QuotePricingSnapshotEntity(
    val quoteId: Long,
    val parameterKey: String,
    val value: String
)
