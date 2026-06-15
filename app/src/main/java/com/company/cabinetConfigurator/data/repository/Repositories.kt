package com.company.cabinetConfigurator.data.repository

import com.example.cabinetconfigurator.data.local.ParameterDefinitionEntity
import com.example.cabinetconfigurator.data.local.PricingDao
import com.example.cabinetconfigurator.data.local.PricingParameterValueEntity
import com.example.cabinetconfigurator.data.local.PricingProfileEntity
import com.example.cabinetconfigurator.data.local.QuoteDao
import com.example.cabinetconfigurator.data.local.QuoteEntity
import com.example.cabinetconfigurator.data.local.QuotePricingSnapshotEntity
import com.example.cabinetconfigurator.data.local.QuoteZoneEntity
import com.example.cabinetconfigurator.domain.model.Accessory
import com.example.cabinetconfigurator.domain.model.AccessoryType
import com.example.cabinetconfigurator.domain.model.ParameterDefinition
import com.example.cabinetconfigurator.domain.model.PricingProfile
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import com.example.cabinetconfigurator.domain.model.FurnitureElement
import com.example.cabinetconfigurator.domain.model.FurniturePiece
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class PricingRepository(private val dao: PricingDao) {
    suspend fun ensureSeedData(defs: List<ParameterDefinition>, values: Map<String, String>) {
        if (dao.countProfiles() > 0) return

        dao.insertDefinitions(defs.map {
            ParameterDefinitionEntity(
                key = it.key,
                label = it.label,
                category = it.category.name,
                valueType = it.valueType.name,
                unit = it.unit,
                sortOrder = it.sortOrder
            )
        })

        val profileId = dao.insertProfile(PricingProfileEntity(name = "Domyślny profil", isActive = true))
        dao.upsertParameterValues(values.map { (k, v) ->
            PricingParameterValueEntity(profileId = profileId, parameterKey = k, value = v)
        })
    }

    suspend fun getActiveProfile(): PricingProfile? = dao.getActiveProfile()?.let { rel ->
        PricingProfile(
            id = rel.profile.id,
            name = rel.profile.name,
            updatedAt = rel.profile.updatedAt,
            values = rel.values.associate { it.parameterKey to it.value }
        )
    }

    fun observeActiveProfile(): Flow<PricingProfile> = dao.observeActiveProfile()
        .filterNotNull()
        .map { rel ->
            PricingProfile(
                id = rel.profile.id,
                name = rel.profile.name,
                updatedAt = rel.profile.updatedAt,
                values = rel.values.associate { it.parameterKey to it.value }
            )
        }

    suspend fun updateParameter(profileId: Long, key: String, value: String) {
        dao.upsertParameterValues(listOf(PricingParameterValueEntity(profileId, key, value)))
    }
}

class QuoteRepository(private val dao: QuoteDao) {
    suspend fun saveQuote(
        draft: QuoteDraft,
        pricingProfileId: Long?,
        pricingProfileName: String?,
        result: QuoteCalculationResult,
        snapshot: Map<String, String>
    ): Long {
        val firstPiece = draft.furniture.firstOrNull()
        val quoteId = dao.insertQuote(
            QuoteEntity(
                name = draft.name.ifBlank { "Wycena ${System.currentTimeMillis()}" },
                cabinetType = draft.cabinetType,
                elementType = draft.elementType,
                widthMm = firstPiece?.widthMm ?: 0,
                heightMm = firstPiece?.heightMm ?: 0,
                depthMm = firstPiece?.depthMm ?: 0,
                totalNet = result.totalNet,
                totalGross = result.totalGross,
                pricingProfileId = pricingProfileId,
                pricingProfileName = pricingProfileName
            )
        )

        val zones = draft.furniture.flatMapIndexed { pIdx, piece ->
            piece.elements.mapIndexed { eIdx, element ->
                QuoteZoneEntity(
                    quoteId = quoteId,
                    name = "${piece.widthMm}x${piece.heightMm}x${piece.depthMm} - ${element.type}",
                    orderIndex = pIdx * 100 + eIdx,
                    quantity = element.quantity,
                    accessoriesJson = element.accessories.toJson()
                )
            }
        }
        dao.insertZones(zones)
        dao.insertSnapshot(snapshot.map { (k, v) -> QuotePricingSnapshotEntity(quoteId, k, v) })
        return quoteId
    }

    fun observeHistory(): Flow<List<Quote>> = dao.observeAllQuotes().map { list ->
        list.map { agg ->
            val elementsByPiece = agg.zones.groupBy { it.orderIndex / 100 }
            val furniture = if (agg.zones.isNotEmpty()) {
                elementsByPiece.entries.sortedBy { it.key }.map { (_, zones) ->
                    val firstParts = zones.firstOrNull()?.name?.split(" - ")?.getOrNull(0)?.trim()
                    val dims = firstParts?.split("x")?.mapNotNull { it.toIntOrNull() }
                    val w = dims?.getOrNull(0) ?: agg.quote.widthMm
                    val h = dims?.getOrNull(1) ?: agg.quote.heightMm
                    val d = dims?.getOrNull(2) ?: agg.quote.depthMm
                    val elements = zones.mapNotNull { zone ->
                        val parts = zone.name.split(" - ").map { it.trim() }
                        if (parts.size >= 2) {
                            val elementType = when (parts[1]) {
                                "FRONT"  -> com.example.cabinetconfigurator.domain.model.ElementType.FRONT
                                "DRAWER" -> com.example.cabinetconfigurator.domain.model.ElementType.DRAWER
                                else     -> null
                            }
                            elementType?.let {
                                FurnitureElement(
                                    type = it,
                                    quantity = zone.quantity,
                                    accessories = zone.accessoriesJson.toAccessoryList()
                                )
                            }
                        } else null
                    }
                    FurniturePiece(widthMm = w, heightMm = h, depthMm = d, elements = elements)
                }
            } else emptyList()
            Quote(
                id = agg.quote.id,
                name = agg.quote.name,
                cabinetType = agg.quote.cabinetType,
                elementType = agg.quote.elementType,
                widthMm = agg.quote.widthMm,
                heightMm = agg.quote.heightMm,
                depthMm = agg.quote.depthMm,
                totalNet = agg.quote.totalNet,
                totalGross = agg.quote.totalGross,
                createdAt = agg.quote.createdAt,
                pricingSnapshot = agg.pricingSnapshot.associate { it.parameterKey to it.value },
                furniture = furniture
            )
        }
    }

    suspend fun deleteQuote(quoteId: Long) {
        dao.deleteQuote(quoteId)
    }
}
