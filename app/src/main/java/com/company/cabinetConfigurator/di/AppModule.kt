package com.company.cabinetConfigurator.di

import android.content.Context
import com.company.cabinetConfigurator.data.local.AppDatabase
import com.company.cabinetConfigurator.data.repository.PricingRepository
import com.company.cabinetConfigurator.data.repository.QuoteRepository
import com.company.cabinetConfigurator.data.repository.TemplateRepository
import com.company.cabinetConfigurator.data.repository.UserSettingsRepository
import com.company.cabinetConfigurator.domain.pdf.PdfGenerator
import com.company.cabinetConfigurator.domain.engine.DefaultCalculationEngine
import com.company.cabinetConfigurator.domain.usecase.CalculateQuoteUseCase
import com.company.cabinetConfigurator.domain.usecase.DeleteQuoteUseCase
import com.company.cabinetConfigurator.domain.usecase.EnsureSeedDataUseCase
import com.company.cabinetConfigurator.domain.usecase.EnsureSeedTemplatesUseCase
import com.company.cabinetConfigurator.domain.usecase.GetActivePricingProfileUseCase
import com.company.cabinetConfigurator.domain.usecase.GetParameterDefinitionsUseCase
import com.company.cabinetConfigurator.domain.usecase.ObserveCabinetTemplatesUseCase
import com.company.cabinetConfigurator.domain.usecase.ObserveQuoteHistoryUseCase
import com.company.cabinetConfigurator.domain.usecase.SaveCabinetTemplateUseCase
import com.company.cabinetConfigurator.domain.usecase.SaveQuoteUseCase
import com.company.cabinetConfigurator.domain.usecase.UpdatePricingParameterUseCase

class AppModule private constructor(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val pricingRepository = PricingRepository(db.pricingDao())
    val quoteRepository = QuoteRepository(db.quoteDao())
    val templateRepository = TemplateRepository(db.templateDao())
    val userSettingsRepository = UserSettingsRepository(context)

    private val calculationEngine = DefaultCalculationEngine()
    val pdfGenerator = PdfGenerator(context)

    val ensureSeedDataUseCase = EnsureSeedDataUseCase(pricingRepository)
    val ensureSeedTemplatesUseCase = EnsureSeedTemplatesUseCase(templateRepository)
    val getParameterDefinitionsUseCase = GetParameterDefinitionsUseCase(pricingRepository)
    val getActivePricingProfileUseCase = GetActivePricingProfileUseCase(pricingRepository)
    val updatePricingParameterUseCase = UpdatePricingParameterUseCase(pricingRepository)
    val calculateQuoteUseCase = CalculateQuoteUseCase(pricingRepository, calculationEngine)
    val saveQuoteUseCase = SaveQuoteUseCase(pricingRepository, quoteRepository, calculateQuoteUseCase)
    val observeQuoteHistoryUseCase = ObserveQuoteHistoryUseCase(quoteRepository)
    val saveCabinetTemplateUseCase = SaveCabinetTemplateUseCase(templateRepository)
    val observeCabinetTemplatesUseCase = ObserveCabinetTemplatesUseCase(templateRepository)
    val deleteQuoteUseCase = DeleteQuoteUseCase(quoteRepository)

    companion object {
        @Volatile private var INSTANCE: AppModule? = null

        fun get(context: Context): AppModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppModule(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
