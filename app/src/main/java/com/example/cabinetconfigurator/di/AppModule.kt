package com.example.cabinetconfigurator.di

import android.content.Context
import com.example.cabinetconfigurator.data.local.AppDatabase
import com.example.cabinetconfigurator.data.repository.PricingRepository
import com.example.cabinetconfigurator.data.repository.QuoteRepository
import com.example.cabinetconfigurator.domain.engine.DefaultCalculationEngine
import com.example.cabinetconfigurator.domain.usecase.CalculateQuoteUseCase
import com.example.cabinetconfigurator.domain.usecase.EnsureSeedDataUseCase
import com.example.cabinetconfigurator.domain.usecase.GetActivePricingProfileUseCase
import com.example.cabinetconfigurator.domain.usecase.ObserveQuoteHistoryUseCase
import com.example.cabinetconfigurator.domain.usecase.GetParameterDefinitionsUseCase
import com.example.cabinetconfigurator.domain.usecase.UpdatePricingParameterUseCase
import com.example.cabinetconfigurator.domain.usecase.SaveQuoteUseCase

class AppModule private constructor(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val pricingRepository = PricingRepository(db.pricingDao())
    val quoteRepository = QuoteRepository(db.quoteDao())

    private val calculationEngine = DefaultCalculationEngine()

    val ensureSeedDataUseCase = EnsureSeedDataUseCase(pricingRepository)
    val getParameterDefinitionsUseCase = GetParameterDefinitionsUseCase(pricingRepository)
    val getActivePricingProfileUseCase = GetActivePricingProfileUseCase(pricingRepository)
    val updatePricingParameterUseCase = UpdatePricingParameterUseCase(pricingRepository)
    val calculateQuoteUseCase = CalculateQuoteUseCase(pricingRepository, calculationEngine)
    val saveQuoteUseCase = SaveQuoteUseCase(pricingRepository, quoteRepository, calculateQuoteUseCase)
    val observeQuoteHistoryUseCase = ObserveQuoteHistoryUseCase(quoteRepository)

    companion object {
        @Volatile private var INSTANCE: AppModule? = null

        fun get(context: Context): AppModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppModule(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
