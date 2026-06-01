package com.example.cabinetconfigurator.domain.engine

import com.example.cabinetconfigurator.domain.model.AccessoryType
import com.example.cabinetconfigurator.domain.model.CalculationLine
import com.example.cabinetconfigurator.domain.model.ElementType
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import com.example.cabinetconfigurator.domain.model.QuoteDraft
import kotlin.math.round

object CalculationKeys {
    const val BOARD_PRICE_PER_M2 = "BOARD_PRICE_PER_M2"
    const val FRONT_PRICE_PER_M2 = "FRONT_PRICE_PER_M2"
    const val LABOR_RATE_PER_HOUR = "LABOR_RATE_PER_HOUR"
    const val LABOR_HOURS_PER_ZONE = "LABOR_HOURS_PER_ZONE"
    const val MARGIN_PERCENT = "MARGIN_PERCENT"
    const val VAT_PERCENT = "VAT_PERCENT"

    fun accessoryPriceKey(manufacturer: String, model: String) = "ACC::$manufacturer::$model"
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
        val laborRatePerHour = decimal(CalculationKeys.LABOR_RATE_PER_HOUR)
        val laborHoursPerZone = decimal(CalculationKeys.LABOR_HOURS_PER_ZONE)
        val marginPercent = decimal(CalculationKeys.MARGIN_PERCENT)
        val vatPercent = decimal(CalculationKeys.VAT_PERCENT)

        var totalBoardArea = 0.0
        var totalFrontArea = 0.0
        var totalZones = 0
        // key: (type, manufacturer, model), value: (totalQty, totalCost)
        val accessoryCostByModel = mutableMapOf<Triple<AccessoryType, String, String>, Pair<Int, Double>>()

        draft.furniture.forEach { piece ->
            val wM = piece.widthMm / 1000.0
            val hM = piece.heightMm / 1000.0
            val dM = piece.depthMm / 1000.0

            totalBoardArea += (2 * hM * dM) + (4 * wM * dM) + (wM * hM)

            piece.elements.forEach { element ->
                totalZones++
                element.accessories.forEach { acc ->
                    val priceKey = CalculationKeys.accessoryPriceKey(acc.manufacturer, acc.model)
                    val price = decimal(priceKey)
                    val key = Triple(acc.type, acc.manufacturer, acc.model)
                    val (prevQty, prevCost) = accessoryCostByModel[key] ?: (0 to 0.0)
                    accessoryCostByModel[key] = (prevQty + acc.quantity) to (prevCost + acc.quantity * price)
                }
                if (element.type == ElementType.FRONT) {
                    val frontsCount = piece.elements.filter { it.type == ElementType.FRONT }.sumOf { it.quantity }
                    val frontHeightM = if (element.heightMm != null) {
                        element.heightMm / 1000.0
                    } else {
                        hM / frontsCount.coerceAtLeast(1)
                    }
                    totalFrontArea += (wM * frontHeightM) * element.quantity
                }
            }
        }

        val boardArea = round2(totalBoardArea)
        val frontArea = round2(totalFrontArea)
        val laborHours = totalZones * laborHoursPerZone

        val boardCost = round2(boardArea * boardPricePerM2)
        val frontCost = round2(frontArea * frontPricePerM2)
        val laborCost = round2(laborHours * laborRatePerHour)

        val accessoryLines = accessoryCostByModel.entries.mapIndexed { i, (key, qtyAndCost) ->
            val (type, mfr, mdl) = key
            val (qty, cost) = qtyAndCost
            val label = if (mfr.isNotBlank() && mdl.isNotBlank()) "$mfr – $mdl" else type.displayName
            val price = decimal(CalculationKeys.accessoryPriceKey(mfr, mdl))
            CalculationLine("ACC_$i", label, round2(cost), qty.toDouble(), "szt", price)
        }

        val totalAccessoryCost = round2(accessoryLines.sumOf { it.amount })
        val subtotal = round2(boardCost + frontCost + totalAccessoryCost + laborCost)
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
            lines = buildList {
                add(CalculationLine("BOARD", "Płyty", boardCost, boardArea, "m²", boardPricePerM2))
                add(CalculationLine("FRONT", "Fronty", frontCost, frontArea, "m²", frontPricePerM2))
                addAll(accessoryLines)
                add(CalculationLine("LABOR", "Robocizna", laborCost, laborHours, "h", laborRatePerHour))
                add(CalculationLine("MARGIN", "Marża", marginAmount, marginPercent, "%", null))
            }
        )
    }

    private fun round2(v: Double): Double = round(v * 100.0) / 100.0
}
