package com.example.cabinetconfigurator.di

import android.content.Context
import com.example.cabinetconfigurator.data.local.AppDatabase
import com.example.cabinetconfigurator.data.repository.PricingRepository
import com.example.cabinetconfigurator.data.repository.QuoteRepository
import com.example.cabinetconfigurator.data.repository.TemplateRepository
import com.example.cabinetconfigurator.data.repository.UserSettingsRepository
import com.example.cabinetconfigurator.domain.pdf.PdfGenerator
import com.example.cabinetconfigurator.domain.engine.DefaultCalculationEngine
import com.example.cabinetconfigurator.domain.usecase.CalculateQuoteUseCase
import com.example.cabinetconfigurator.domain.usecase.DeleteQuoteUseCase
import com.example.cabinetconfigurator.domain.usecase.EnsureSeedDataUseCase
import com.example.cabinetconfigurator.domain.usecase.EnsureSeedTemplatesUseCase
import com.example.cabinetconfigurator.domain.usecase.GetActivePricingProfileUseCase
import com.example.cabinetconfigurator.domain.usecase.GetParameterDefinitionsUseCase
import com.example.cabinetconfigurator.domain.usecase.ObserveCabinetTemplatesUseCase
import com.example.cabinetconfigurator.domain.usecase.ObserveQuoteHistoryUseCase
import com.example.cabinetconfigurator.domain.usecase.SaveCabinetTemplateUseCase
import com.example.cabinetconfigurator.domain.usecase.SaveQuoteUseCase
import com.example.cabinetconfigurator.domain.usecase.UpdatePricingParameterUseCase

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
