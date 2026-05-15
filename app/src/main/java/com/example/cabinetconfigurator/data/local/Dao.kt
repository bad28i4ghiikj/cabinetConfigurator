package com.example.cabinetconfigurator.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PricingDao {
    @Query("SELECT * FROM parameter_definitions ORDER BY sortOrder")
    suspend fun getDefinitions(): List<ParameterDefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinitions(items: List<ParameterDefinitionEntity>)

    @Insert
    suspend fun insertProfile(profile: PricingProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParameterValues(items: List<PricingParameterValueEntity>)

    @Transaction
    @Query("SELECT * FROM pricing_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(): PricingProfileWithValues?

    @Transaction
    @Query("SELECT * FROM pricing_profiles WHERE isActive = 1 LIMIT 1")
    fun observeActiveProfile(): Flow<PricingProfileWithValues?>

    @Query("SELECT COUNT(*) FROM pricing_profiles")
    suspend fun countProfiles(): Int
}

@Dao
interface QuoteDao {
    @Insert
    suspend fun insertQuote(entity: QuoteEntity): Long

    @Insert
    suspend fun insertZones(items: List<QuoteZoneEntity>)

    @Insert
    suspend fun insertSnapshot(items: List<QuotePricingSnapshotEntity>)

    @Transaction
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun observeAllQuotes(): Flow<List<QuoteAggregate>>
}
