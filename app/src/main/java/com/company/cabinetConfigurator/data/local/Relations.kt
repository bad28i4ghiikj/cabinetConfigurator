package com.company.cabinetConfigurator.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class PricingProfileWithValues(
    @Embedded val profile: PricingProfileEntity,
    @Relation(parentColumn = "id", entityColumn = "profileId")
    val values: List<PricingParameterValueEntity>
)

data class QuoteAggregate(
    @Embedded val quote: QuoteEntity,
    @Relation(parentColumn = "id", entityColumn = "quoteId")
    val zones: List<QuoteZoneEntity>,
    @Relation(parentColumn = "id", entityColumn = "quoteId")
    val pricingSnapshot: List<QuotePricingSnapshotEntity>
)

data class CabinetTemplateWithElements(
    @Embedded val template: CabinetTemplateEntity,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val elements: List<CabinetTemplateElementEntity>
)
