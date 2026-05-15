package com.example.cabinetconfigurator.domain.engine

import com.example.cabinetconfigurator.domain.model.CalculationLine
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import kotlin.math.round

object CalculationKeys {
    const val BOARD_PRICE_PER_M2 = "BOARD_PRICE_PER_M2"
    const val FRONT_PRICE_PER_M2 = "FRONT_PRICE_PER_M2"
    const val HINGE_PRICE_PER_PCS = "HINGE_PRICE_PER_PCS"
    const val DRAWER_RUNNER_PRICE_PER_PCS = "DRAWER_RUNNER_PRICE_PER_PCS"
    const val LABOR_RATE_PER_HOUR = "LABOR_RATE_PER_HOUR"
    const val LABOR_HOURS_PER_ZONE = "LABOR_HOURS_PER_ZONE"
    const val MARGIN_PERCENT = "MARGIN_PERCENT"
    const val VAT_PERCENT = "VAT_PERCENT"
}

interface CalculationEngine {
    fun calculate(draft: QuoteDraft, pricingValues: Map<String, String>): QuoteCalculationResult
}

class DefaultCalculationEngine : CalculationEngine {
    override fun calculate(draft: QuoteDraft, pricingValues: Map<String, String>): QuoteCalculationResult {
        fun decimal(key: String): Double =
            pricingValues[key]?.replace(",", ".")?.toDoubleOrNull() ?: 0.0

        val boardPricePerM2 = decimal(CalculationKeys.BOARD_PRICE_PER_M2)
        val frontPricePerM2 = decimal(CalculationKeys.FRONT_PRICE_PER_M2)
        val hingePricePerPcs = decimal(CalculationKeys.HINGE_PRICE_PER_PCS)
        val drawerRunnerPricePerPcs = decimal(CalculationKeys.DRAWER_RUNNER_PRICE_PER_PCS)
        val laborRatePerHour = decimal(CalculationKeys.LABOR_RATE_PER_HOUR)
        val laborHoursPerZone = decimal(CalculationKeys.LABOR_HOURS_PER_ZONE)
        val marginPercent = decimal(CalculationKeys.MARGIN_PERCENT)
        val vatPercent = decimal(CalculationKeys.VAT_PERCENT)

        var totalBoardArea = 0.0
        var totalFrontArea = 0.0
        var totalHingeCount = 0
        var totalDrawerRunnerCount = 0
        var totalZones = 0

        draft.furniture.forEach { piece ->
            val wM = piece.widthMm / 1000.0
            val hM = piece.heightMm / 1000.0
            val dM = piece.depthMm / 1000.0

            // Simple cabinet calculation: 2 sides, top, bottom, back, shelves
            // For now, let's keep the user's formula but applied per piece
            totalBoardArea += (2 * hM * dM) + (4 * wM * dM) + (wM * hM)
            
            piece.elements.forEach { element ->
                totalZones++
                if (element.type == ElementType.FRONT) {
                    totalFrontArea += (wM * hM) / piece.elements.count { it.type == ElementType.FRONT }.coerceAtLeast(1)
                    totalHingeCount += element.hingeCount
                } else if (element.type == ElementType.DRAWER) {
                    totalDrawerRunnerCount += element.drawerRunnerCount
                }
            }
        }

        val boardArea = round2(totalBoardArea)
        val frontArea = round2(totalFrontArea)
        val hingeCount = totalHingeCount
        val drawerRunnerCount = totalDrawerRunnerCount
        val laborHours = totalZones * laborHoursPerZone

        val boardCost = round2(boardArea * boardPricePerM2)
        val frontCost = round2(frontArea * frontPricePerM2)
        val hingeCost = round2(hingeCount * hingePricePerPcs)
        val drawerCost = round2(drawerRunnerCount * drawerRunnerPricePerPcs)
        val laborCost = round2(laborHours * laborRatePerHour)
        val subtotal = round2(boardCost + frontCost + hingeCost + drawerCost + laborCost)
        val marginAmount = round2(subtotal * (marginPercent / 100.0))
        val totalNet = round2(subtotal + marginAmount)
        val totalGross = round2(totalNet * (1.0 + vatPercent / 100.0))

        val warnings = buildList {
            if (boardPricePerM2 <= 0.0) add("Brak BOARD_PRICE_PER_M2")
            if (frontPricePerM2 <= 0.0) add("Brak FRONT_PRICE_PER_M2")
            if (laborRatePerHour <= 0.0) add("Brak LABOR_RATE_PER_HOUR")
            if (vatPercent <= 0.0) add("Brak VAT_PERCENT")
        }

        return QuoteCalculationResult(
            totalNet = totalNet,
            totalGross = totalGross,
            warnings = warnings,
            lines = listOf(
                CalculationLine("BOARD", "Płyty", boardCost, boardArea, "m²", boardPricePerM2),
                CalculationLine("FRONT", "Fronty", frontCost, frontArea, "m²", frontPricePerM2),
                CalculationLine("HINGE", "Zawiasy", hingeCost, hingeCount.toDouble(), "szt", hingePricePerPcs),
                CalculationLine("DRAWER", "Prowadnice", drawerCost, drawerRunnerCount.toDouble(), "szt", drawerRunnerPricePerPcs),
                CalculationLine("LABOR", "Robocizna", laborCost, laborHours, "h", laborRatePerHour),
                CalculationLine("MARGIN", "Marża", marginAmount, marginPercent, "%", null)
            )
        )
    }

    private fun round2(v: Double): Double = round(v * 100.0) / 100.0
}
