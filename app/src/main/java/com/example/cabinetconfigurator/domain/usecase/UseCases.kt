package com.example.cabinetconfigurator.domain.usecase

import com.example.cabinetconfigurator.data.repository.PricingRepository
import com.example.cabinetconfigurator.data.repository.QuoteRepository
import com.example.cabinetconfigurator.domain.engine.CalculationEngine
import com.example.cabinetconfigurator.domain.engine.CalculationKeys
import com.example.cabinetconfigurator.domain.engine.CalculationKeys.DRAWER_RUNNER_PRICE_PER_PCS
import com.example.cabinetconfigurator.domain.model.ParameterDefinition
import com.example.cabinetconfigurator.domain.model.ParameterValueType
import com.example.cabinetconfigurator.domain.model.PricingCategory
import com.example.cabinetconfigurator.domain.model.PricingProfile
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import kotlinx.coroutines.flow.Flow

class EnsureSeedDataUseCase(private val pricingRepository: PricingRepository) {
    suspend operator fun invoke() {
        val defs = listOf(
            ParameterDefinition(CalculationKeys.BOARD_PRICE_PER_M2, "Płyta zł/m²", PricingCategory.MATERIALS, ParameterValueType.DECIMAL, "zł/m²", 1),
            ParameterDefinition(CalculationKeys.FRONT_PRICE_PER_M2, "Front zł/m²", PricingCategory.FRONTS, ParameterValueType.DECIMAL, "zł/m²", 2),
            ParameterDefinition(CalculationKeys.HINGE_PRICE_PER_PCS, "Zawias zł/szt", PricingCategory.ACCESSORIES, ParameterValueType.DECIMAL, "zł/szt", 3),
            ParameterDefinition(CalculationKeys.DRAWER_RUNNER_PRICE_PER_PCS, "Prowadnica zł/szt", PricingCategory.ACCESSORIES, ParameterValueType.DECIMAL, "zł/kpl", 4),
            ParameterDefinition(CalculationKeys.LABOR_RATE_PER_HOUR, "Robocizna zł/h", PricingCategory.LABOR, ParameterValueType.DECIMAL, "zł/h", 5),
            ParameterDefinition(CalculationKeys.LABOR_HOURS_PER_ZONE, "Godzin na strefę", PricingCategory.LABOR, ParameterValueType.DECIMAL, "h", 6),
            ParameterDefinition(CalculationKeys.MARGIN_PERCENT, "Marża %", PricingCategory.MARGIN, ParameterValueType.PERCENT, "%", 7),
            ParameterDefinition(CalculationKeys.VAT_PERCENT, "VAT %", PricingCategory.GENERAL, ParameterValueType.PERCENT, "%", 8)
        )
        val defaults = mapOf(
            CalculationKeys.BOARD_PRICE_PER_M2 to "150",
            CalculationKeys.FRONT_PRICE_PER_M2 to "220",
            CalculationKeys.HINGE_PRICE_PER_PCS to "12",
            CalculationKeys.DRAWER_RUNNER_PRICE_PER_PCS to "35",
            CalculationKeys.LABOR_RATE_PER_HOUR to "80",
            CalculationKeys.LABOR_HOURS_PER_ZONE to "0.5",
            CalculationKeys.MARGIN_PERCENT to "20",
            CalculationKeys.VAT_PERCENT to "23"
        )
        pricingRepository.ensureSeedData(defs, defaults)
    }
}

class GetParameterDefinitionsUseCase(private val pricingRepository: PricingRepository) {
    suspend operator fun invoke(): List<ParameterDefinition> {
        val defs = listOf(
            ParameterDefinition(CalculationKeys.BOARD_PRICE_PER_M2, "Płyta zł/m²", PricingCategory.MATERIALS, ParameterValueType.DECIMAL, "zł/m²", 1),
            ParameterDefinition(CalculationKeys.FRONT_PRICE_PER_M2, "Front zł/m²", PricingCategory.FRONTS, ParameterValueType.DECIMAL, "zł/m²", 2),
            ParameterDefinition(CalculationKeys.HINGE_PRICE_PER_PCS, "Zawias zł/szt", PricingCategory.ACCESSORIES, ParameterValueType.DECIMAL, "zł/szt", 3),
            ParameterDefinition(CalculationKeys.DRAWER_RUNNER_PRICE_PER_PCS, "Prowadnica zł/szt", PricingCategory.ACCESSORIES, ParameterValueType.DECIMAL, "zł/kpl", 4),
            ParameterDefinition(CalculationKeys.LABOR_RATE_PER_HOUR, "Robocizna zł/h", PricingCategory.LABOR, ParameterValueType.DECIMAL, "zł/h", 5),
            ParameterDefinition(CalculationKeys.LABOR_HOURS_PER_ZONE, "Godzin na strefę", PricingCategory.LABOR, ParameterValueType.DECIMAL, "h", 6),
            ParameterDefinition(CalculationKeys.MARGIN_PERCENT, "Marża %", PricingCategory.MARGIN, ParameterValueType.PERCENT, "%", 7),
            ParameterDefinition(CalculationKeys.VAT_PERCENT, "VAT %", PricingCategory.GENERAL, ParameterValueType.PERCENT, "%", 8)
        )
        return defs
    }
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
