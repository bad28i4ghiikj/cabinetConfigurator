package com.example.cabinetconfigurator.domain.model

import java.util.UUID

enum class PricingCategory {
    MATERIALS,
    FRONTS,
    ACCESSORIES,
    LABOR,
    EXTRA,
    MARGIN,
    GENERAL
}

enum class ParameterValueType {
    DECIMAL,
    INT,
    BOOL,
    TEXT,
    PERCENT
}

data class ParameterDefinition(
    val key: String,
    val label: String,
    val category: PricingCategory,
    val valueType: ParameterValueType,
    val unit: String? = null,
    val sortOrder: Int = 0
)

data class PricingProfile(
    val id: Long,
    val name: String,
    val values: Map<String, String>,
    val updatedAt: Long
)

enum class AccessoryType(val displayName: String) {
    HINGE("Zawias"),
    DRAWER_RUNNER("Prowadnica"),
    LIFT_SYSTEM("Podnośnik"),
    CARGO_SYSTEM("Cargo"),
    TIP_ON("Tip-On"),
    SOFT_CLOSE("Domykacz"),
    LED_LIGHTING("Oświetlenie LED")
}

data class Accessory(
    val id: String = UUID.randomUUID().toString(),
    val type: AccessoryType,
    val quantity: Int = 1,
    val manufacturer: String = "",
    val model: String = ""
)

data class FurnitureElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType,
    val quantity: Int = 1,
    val accessories: List<Accessory> = emptyList(),
    val widthMm: Int? = null,
    val heightMm: Int? = null,
    val depthMm: Int? = null
)

enum class ElementType(val displayName: String, val defaultAccessoryTypes: List<AccessoryType>) {
    FRONT("Front zawiasowy", listOf(AccessoryType.HINGE)),
    DRAWER("Szuflada", listOf(AccessoryType.DRAWER_RUNNER)),
    OVEN("Piekarnik", emptyList()),
    MICROWAVE("Mikrofala", emptyList()),
    DISHWASHER("Zmywarka", emptyList()),
    FRIDGE("Lodówka", emptyList()),
    LIFT_UP("HK-S / Podnośnik", listOf(AccessoryType.LIFT_SYSTEM)),
    SHELF("Półka", emptyList()),
    CARGO("Cargo", listOf(AccessoryType.CARGO_SYSTEM)),
    INNER_DRAWER("Szuflada wewnętrzna", listOf(AccessoryType.DRAWER_RUNNER))
}

enum class CabinetType(val displayName: String) {
    BASE("Szafka dolna"),
    WALL("Szafka górna"),
    TALL("Słupek / Komin"),
    CORNER("Szafka narożna")
}

data class FurniturePiece(
    val id: String = UUID.randomUUID().toString(),
    val cabinetType: CabinetType = CabinetType.BASE,
    val widthMm: Int = 600,
    val heightMm: Int = 720,
    val depthMm: Int = 560,
    val elements: List<FurnitureElement> = emptyList()
)

data class QuoteDraft(
    val name: String = "",
    val cabinetType: String = "Szafka dolna",
    val elementType: String = "Dolna",
    val furniture: List<FurniturePiece> = listOf(
        FurniturePiece(widthMm = 600, heightMm = 720, depthMm = 560)
    )
)

data class CabinetTemplate(
    val id: Long = 0,
    val name: String,
    val cabinetType: String,
    val widthMm: Int = 600,
    val heightMm: Int = 720,
    val depthMm: Int = 560,
    val elements: List<FurnitureElement> = emptyList()
)

data class CalculationLine(
    val code: String,
    val label: String,
    val amount: Double,
    val quantity: Double? = null,
    val unit: String? = null,
    val unitPrice: Double? = null
)

data class QuoteCalculationResult(
    val totalNet: Double,
    val totalGross: Double,
    val lines: List<CalculationLine>,
    val warnings: List<String> = emptyList()
)

data class Quote(
    val id: Long,
    val name: String,
    val cabinetType: String,
    val elementType: String,
    val widthMm: Int,
    val heightMm: Int,
    val depthMm: Int,
    val totalNet: Double,
    val totalGross: Double,
    val createdAt: Long,
    val pricingSnapshot: Map<String, String>,
    val furniture: List<FurniturePiece> = emptyList()
)
