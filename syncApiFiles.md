
CabinetConfigurator.SyncApi
│
├── Controllers
│   └── SyncController.cs
│
├── Data
│   ├── SyncDbContext.cs
│   └── Entities
│       ├── UserProfileEntity.cs
│       ├── UserPricingProfileEntity.cs
│       ├── UserPricingParameterEntity.cs
│       ├── UserQuoteEntity.cs
│       ├── UserQuoteZoneEntity.cs
│       ├── UserQuoteSnapshotEntity.cs
│       └── DeviceSyncStateEntity.cs
│
├── Dtos
│   ├── SyncPushRequest.cs
│   ├── SyncPushResponse.cs
│   ├── SyncPullRequest.cs
│   ├── SyncPullResponse.cs
│   ├── PricingProfileDto.cs
│   └── QuoteDto.cs
│
├── Services
│   └── SyncService.cs
│
├── Program.cs
└── appsettings.json
