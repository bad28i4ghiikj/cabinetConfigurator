package com.company.cabinetConfigurator.domain.model

data class AccessoryCatalogEntry(
    val manufacturer: String,
    val type: AccessoryType,
    val model: String
)

object AccessoryCatalog {
    val manufacturers = listOf("Blum", "Grass", "Häfele", "Hettich", "GTV", "Producent X")

    private val entries: List<AccessoryCatalogEntry> = listOf(
        // Blum
        AccessoryCatalogEntry("Blum", AccessoryType.HINGE, "Clip Top Blumotion 110°"),
        AccessoryCatalogEntry("Blum", AccessoryType.HINGE, "Clip Top 110°"),
        AccessoryCatalogEntry("Blum", AccessoryType.DRAWER_RUNNER, "Tandem 500mm"),
        AccessoryCatalogEntry("Blum", AccessoryType.DRAWER_RUNNER, "Tandem 450mm"),
        AccessoryCatalogEntry("Blum", AccessoryType.DRAWER_RUNNER, "Tandem 400mm"),
        AccessoryCatalogEntry("Blum", AccessoryType.LIFT_SYSTEM, "Aventos HK"),
        AccessoryCatalogEntry("Blum", AccessoryType.LIFT_SYSTEM, "Aventos HL"),
        AccessoryCatalogEntry("Blum", AccessoryType.LIFT_SYSTEM, "Aventos HF"),
        AccessoryCatalogEntry("Blum", AccessoryType.SOFT_CLOSE, "Blumotion"),
        AccessoryCatalogEntry("Blum", AccessoryType.TIP_ON, "Tip-On"),
        // Grass
        AccessoryCatalogEntry("Grass", AccessoryType.HINGE, "Tiomos 110°"),
        AccessoryCatalogEntry("Grass", AccessoryType.HINGE, "Tiomos Soft-Close 110°"),
        AccessoryCatalogEntry("Grass", AccessoryType.DRAWER_RUNNER, "Dynapro 500mm"),
        AccessoryCatalogEntry("Grass", AccessoryType.DRAWER_RUNNER, "Dynapro 450mm"),
        AccessoryCatalogEntry("Grass", AccessoryType.DRAWER_RUNNER, "Dynapro 400mm"),
        AccessoryCatalogEntry("Grass", AccessoryType.SOFT_CLOSE, "Soft-Close"),
        // Häfele
        AccessoryCatalogEntry("Häfele", AccessoryType.HINGE, "Metalla 110°"),
        AccessoryCatalogEntry("Häfele", AccessoryType.HINGE, "Metalla Soft-Close 110°"),
        AccessoryCatalogEntry("Häfele", AccessoryType.DRAWER_RUNNER, "Matrix Box 500mm"),
        AccessoryCatalogEntry("Häfele", AccessoryType.DRAWER_RUNNER, "Matrix Box 450mm"),
        AccessoryCatalogEntry("Häfele", AccessoryType.SOFT_CLOSE, "Free Flap 1.5"),
        AccessoryCatalogEntry("Häfele", AccessoryType.LED_LIGHTING, "Loox LED 3000K"),
        AccessoryCatalogEntry("Häfele", AccessoryType.LED_LIGHTING, "Loox LED 4000K"),
        // Hettich
        AccessoryCatalogEntry("Hettich", AccessoryType.HINGE, "Sensys 8631"),
        AccessoryCatalogEntry("Hettich", AccessoryType.HINGE, "Sensys 8646"),
        AccessoryCatalogEntry("Hettich", AccessoryType.DRAWER_RUNNER, "ArciTech 500mm"),
        AccessoryCatalogEntry("Hettich", AccessoryType.DRAWER_RUNNER, "ArciTech 450mm"),
        AccessoryCatalogEntry("Hettich", AccessoryType.DRAWER_RUNNER, "ArciTech 400mm"),
        AccessoryCatalogEntry("Hettich", AccessoryType.SOFT_CLOSE, "Inmotion"),
        // GTV
        AccessoryCatalogEntry("GTV", AccessoryType.HINGE, "H-0 110°"),
        AccessoryCatalogEntry("GTV", AccessoryType.HINGE, "H-0 Soft-Close 110°"),
        AccessoryCatalogEntry("GTV", AccessoryType.DRAWER_RUNNER, "Standard 500mm"),
        AccessoryCatalogEntry("GTV", AccessoryType.DRAWER_RUNNER, "Standard 450mm"),
        AccessoryCatalogEntry("GTV", AccessoryType.CARGO_SYSTEM, "Cargo Classic"),
        AccessoryCatalogEntry("GTV", AccessoryType.LED_LIGHTING, "LED 12V 3000K"),
        // Producent X
        AccessoryCatalogEntry("Producent X", AccessoryType.HINGE, "Standard 110°"),
        AccessoryCatalogEntry("Producent X", AccessoryType.DRAWER_RUNNER, "Standard 500mm"),
        AccessoryCatalogEntry("Producent X", AccessoryType.SOFT_CLOSE, "Standard"),
    )

    fun entriesFor(manufacturer: String): List<AccessoryCatalogEntry> =
        entries.filter { it.manufacturer == manufacturer }

    fun modelsFor(manufacturer: String): List<String> =
        entriesFor(manufacturer).map { it.model }

    fun allowedAccessoryTypesFor(elementType: ElementType): List<AccessoryType> =
        elementType.defaultAccessoryTypes.ifEmpty { AccessoryType.entries }
}
