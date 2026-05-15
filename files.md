
com.example.cabinetconfigurator
│
├── data
│   ├── local
│   │   ├── AppDatabase.kt
│   │   ├── dao
│   │   │   ├── PricingDao.kt
│   │   │   ├── QuoteDao.kt
│   │   │   └── SyncDao.kt
│   │   ├── entities
│   │   │   ├── ParameterDefinitionEntity.kt
│   │   │   ├── PricingProfileEntity.kt
│   │   │   ├── PricingParameterValueEntity.kt
│   │   │   ├── QuoteEntity.kt
│   │   │   ├── QuoteZoneEntity.kt
│   │   │   ├── QuotePricingSnapshotEntity.kt
│   │   │   ├── SyncQueueEntity.kt
│   │   │   └── RemoteMappingEntity.kt
│   │   └── relations
│   │       ├── PricingProfileWithValues.kt
│   │       └── QuoteAggregate.kt
│   │
│   ├── mapper
│   │   ├── PricingMapper.kt
│   │   └── QuoteMapper.kt
│   │
│   ├── remote
│   │   ├── api
│   │   │   └── SyncApi.kt
│   │   └── dto
│   │       └── SyncDtos.kt
│   │
│   └── repository
│       ├── PricingRepository.kt
│       ├── QuoteRepository.kt
│       └── SyncRepository.kt
│
├── domain
│   ├── engine
│   │   ├── CalculationEngine.kt
│   │   ├── DefaultCalculationEngine.kt
│   │   ├── CalculationContext.kt
│   │   ├── CalculationBreakdown.kt
│   │   └── ParameterResolver.kt
│   │
│   ├── model
│   │   ├── PricingCategory.kt
│   │   ├── ParameterValueType.kt
│   │   ├── ParameterDefinition.kt
│   │   ├── PricingProfile.kt
│   │   ├── QuoteDraft.kt
│   │   ├── Quote.kt
│   │   ├── QuoteZone.kt
│   │   ├── QuoteCalculationResult.kt
│   │   └── SyncState.kt
│   │
│   └── usecase
│       ├── GetActivePricingProfileUseCase.kt
│       ├── UpdatePricingParameterUseCase.kt
│       ├── CalculateQuoteUseCase.kt
│       ├── SaveQuoteUseCase.kt
│       └── SyncPendingUseCase.kt
│
├── worker
│   └── SyncWorker.kt
│
├── di
│   └── AppModule.kt
│
├── ui
│   ├── viewmodel
│   │   ├── PricingViewModel.kt
│   │   ├── ConfiguratorViewModel.kt
│   │   └── QuoteHistoryViewModel.kt
│   └── screens
│       ├── PricingProfileScreen.kt
│       ├── ConfiguratorScreen.kt
│       └── QuoteHistoryScreen.kt
│
├── MainActivity.kt
└── CabinetConfiguratorApp.kt
