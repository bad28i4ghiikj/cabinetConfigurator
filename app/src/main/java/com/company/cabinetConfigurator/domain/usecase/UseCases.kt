package com.company.cabinetConfigurator.domain.usecase

import com.company.cabinetConfigurator.data.repository.PricingRepository
import com.company.cabinetConfigurator.data.repository.QuoteRepository
import com.company.cabinetConfigurator.data.repository.TemplateRepository
import com.company.cabinetConfigurator.domain.engine.CalculationEngine
import com.company.cabinetConfigurator.domain.engine.CalculationKeys
import com.company.cabinetConfigurator.domain.model.ParameterDefinition
import com.company.cabinetConfigurator.domain.model.ParameterValueType
import com.company.cabinetConfigurator.domain.model.PricingCategory
import com.company.cabinetConfigurator.domain.model.PricingProfile
import com.company.cabinetConfigurator.domain.model.CabinetTemplate
import com.company.cabinetConfigurator.domain.model.Quote
import com.company.cabinetConfigurator.domain.model.QuoteCalculationResult
import com.company.cabinetConfigurator.domain.model.QuoteDraft
import kotlinx.coroutines.flow.Flow

internal val PRICING_PARAMETER_DEFINITIONS = listOf(
    ParameterDefinition(CalculationKeys.BOARD_PRICE_PER_M2, "Płyta zł/m²", PricingCategory.MATERIALS, ParameterValueType.DECIMAL, "zł/m²", 1),
    ParameterDefinition(CalculationKeys.FRONT_PRICE_PER_M2, "Front zł/m²", PricingCategory.FRONTS, ParameterValueType.DECIMAL, "zł/m²", 2),
    ParameterDefinition(CalculationKeys.LABOR_RATE_PER_HOUR, "Robocizna zł/h", PricingCategory.LABOR, ParameterValueType.DECIMAL, "zł/h", 3),
    ParameterDefinition(CalculationKeys.LABOR_HOURS_PER_ZONE, "Godzin na strefę", PricingCategory.LABOR, ParameterValueType.DECIMAL, "h", 4),
    ParameterDefinition(CalculationKeys.MARGIN_PERCENT, "Marża %", PricingCategory.MARGIN, ParameterValueType.PERCENT, "%", 5),
    ParameterDefinition(CalculationKeys.VAT_PERCENT, "VAT %", PricingCategory.GENERAL, ParameterValueType.PERCENT, "%", 6)
)

class EnsureSeedDataUseCase(private val pricingRepository: PricingRepository) {
    suspend operator fun invoke() {
        val defaults = mapOf(
            CalculationKeys.BOARD_PRICE_PER_M2 to "150",
            CalculationKeys.FRONT_PRICE_PER_M2 to "220",
            CalculationKeys.LABOR_RATE_PER_HOUR to "80",
            CalculationKeys.LABOR_HOURS_PER_ZONE to "0.5",
            CalculationKeys.MARGIN_PERCENT to "20",
            CalculationKeys.VAT_PERCENT to "23"
        )
        pricingRepository.ensureSeedData(PRICING_PARAMETER_DEFINITIONS, defaults)
    }
}

class GetParameterDefinitionsUseCase(private val pricingRepository: PricingRepository) {
    suspend operator fun invoke(): List<ParameterDefinition> = PRICING_PARAMETER_DEFINITIONS
}

class GetActivePricingProfileUseCase(private val pricingRepository: PricingRepository) {
    operator fun invoke(): Flow<PricingProfile> = pricingRepository.observeActiveProfile()
}

class UpdatePricingParameterUseCase(private val pricingRepository: PricingRepository) {
    suspend operator fun invoke(profileId: Long, key: String, value: String) {
        pricingRepository.updateParameter(profileId, key, value)
    }
}

class CalculateQuoteUseCase(
    private val pricingRepository: PricingRepository,
    private val calculationEngine: CalculationEngine
) {
    suspend operator fun invoke(draft: QuoteDraft): Pair<Map<String, String>, QuoteCalculationResult> {
        val profile = pricingRepository.getActiveProfile() ?: error("Brak aktywnego profilu")
        val snapshot = profile.values.toMap()
        return snapshot to calculationEngine.calculate(draft, snapshot)
    }
}

class SaveQuoteUseCase(
    private val pricingRepository: PricingRepository,
    private val quoteRepository: QuoteRepository,
    private val calculateQuoteUseCase: CalculateQuoteUseCase
) {
    suspend operator fun invoke(draft: QuoteDraft): Long {
        val profile = pricingRepository.getActiveProfile() ?: error("Brak aktywnego profilu")
        val (snapshot, result) = calculateQuoteUseCase(draft)
        return quoteRepository.saveQuote(draft, profile.id, profile.name, result, snapshot)
    }
}

class ObserveQuoteHistoryUseCase(private val quoteRepository: QuoteRepository) {
    operator fun invoke(): Flow<List<Quote>> = quoteRepository.observeHistory()
}

class DeleteQuoteUseCase(private val quoteRepository: QuoteRepository) {
    suspend operator fun invoke(quoteId: Long) {
        quoteRepository.deleteQuote(quoteId)
    }
}

class ObserveCabinetTemplatesUseCase(private val templateRepository: TemplateRepository) {
    operator fun invoke(): Flow<List<CabinetTemplate>> = templateRepository.observeTemplates()
}

class SaveCabinetTemplateUseCase(private val templateRepository: TemplateRepository) {
    suspend operator fun invoke(template: CabinetTemplate): Long = templateRepository.saveTemplate(template)
}

class EnsureSeedTemplatesUseCase(private val templateRepository: TemplateRepository) {
    suspend operator fun invoke() = templateRepository.ensureSeedTemplates()
}
