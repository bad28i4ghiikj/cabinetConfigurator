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

data class FurnitureElement(
    val id: String = UUID.randomUUID().toString(),
    val type: ElementType,
    val quantity: Int = 1,
    val hingeCount: Int = 0,
    val drawerRunnerCount: Int = 0
)

enum class ElementType {
    FRONT,
    DRAWER
}

data class FurniturePiece(
    val id: String = UUID.randomUUID().toString(),
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
    val pricingSnapshot: Map<String, String>
)
