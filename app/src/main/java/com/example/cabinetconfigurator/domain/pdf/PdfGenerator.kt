package com.example.cabinetconfigurator.domain.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.cabinetconfigurator.domain.model.Quote
import com.example.cabinetconfigurator.domain.model.QuoteCalculationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfGenerator(private val context: Context) {

    private val locale = Locale("pl", "PL")
    private val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", locale)

    suspend fun generateQuotePdf(
        quote: Quote,
        calculation: QuoteCalculationResult,
        logoUri: Uri?,
        companyName: String?
    ): File = withContext(Dispatchers.IO) {
        val logoBitmap = logoUri?.let { uri ->
            runCatching {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(uri).build()
                val result = loader.execute(request)
                (result as? SuccessResult)?.drawable?.let { drawable ->
                    android.graphics.Bitmap.createBitmap(
                        200, 80, android.graphics.Bitmap.Config.ARGB_8888
                    ).also { bmp ->
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, bmp.width, bmp.height)
                        drawable.draw(canvas)
                    }
                }
            }.getOrNull()
        }

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val contentWidth = pageWidth - 2 * margin

        val paintTitle = Paint().apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 16f
            color = Color.BLACK
        }
        val paintNormal = Paint().apply {
            typeface = Typeface.DEFAULT
            textSize = 10f
            color = Color.BLACK
        }
        val paintBold = Paint().apply {
            typeface = Typeface.DEFAULT_BOLD
            textSize = 10f
            color = Color.BLACK
        }
        val paintSmall = Paint().apply {
            typeface = Typeface.DEFAULT
            textSize = 8f
            color = Color.DKGRAY
        }
        val paintLine = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var pageIndex = 0
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageIndex).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var y = margin

        fun newPage() {
            document.finishPage(page)
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageIndex).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = margin
        }

        fun checkSpace(needed: Float) {
            if (y + needed > pageHeight - margin - 20f) newPage()
        }

        fun drawLine() {
            canvas.drawLine(margin, y, margin + contentWidth, y, paintLine)
            y += 6f
        }

        // Header
        logoBitmap?.let { bmp ->
            canvas.drawBitmap(bmp, margin, y, null)
            y += 90f
        }

        companyName?.let {
            canvas.drawText(it, margin, y, paintBold)
            y += 16f
        }

        canvas.drawText("WYCENA: ${quote.name.ifBlank { "#${quote.id}" }}", margin, y, paintTitle)
        y += 22f
        canvas.drawText("Data: ${dateFormat.format(Date(quote.createdAt))}", margin, y, paintSmall)
        y += 18f
        drawLine()

        // Furniture list
        if (quote.furniture.isNotEmpty()) {
            checkSpace(20f)
            canvas.drawText("MEBLE:", margin, y, paintBold)
            y += 14f
            quote.furniture.forEachIndexed { idx, piece ->
                checkSpace(14f)
                val pieceName = piece.name.ifBlank { "Mebel ${idx + 1}" }
                canvas.drawText(
                    "  $pieceName — ${piece.cabinetType.displayName}  ${piece.widthMm}×${piece.heightMm}×${piece.depthMm} mm",
                    margin, y, paintNormal
                )
                y += 12f
                piece.elements.forEach { elem ->
                    checkSpace(12f)
                    canvas.drawText(
                        "    • ${elem.type.displayName} ×${elem.quantity}",
                        margin, y, paintSmall
                    )
                    y += 11f
                }
            }
            y += 6f
            drawLine()
        }

        // Calculation lines
        checkSpace(20f)
        canvas.drawText("KALKULACJA:", margin, y, paintBold)
        y += 14f

        calculation.lines.forEach { line ->
            checkSpace(12f)
            val qty = if (line.quantity != null) " ×${line.quantity} ${line.unit.orEmpty()}" else ""
            val price = if (line.unitPrice != null) " @ ${currencyFormat.format(line.unitPrice)}" else ""
            canvas.drawText("  ${line.label}$qty$price", margin, y, paintNormal)
            canvas.drawText(currencyFormat.format(line.amount), margin + contentWidth, y, paintNormal.apply {
                textAlign = Paint.Align.RIGHT
            })
            y += 12f
        }
        paintNormal.textAlign = Paint.Align.LEFT

        y += 4f
        drawLine()

        // Totals
        checkSpace(30f)
        canvas.drawText("Netto:", margin, y, paintBold)
        canvas.drawText(currencyFormat.format(calculation.totalNet), margin + contentWidth, y, paintBold.apply {
            textAlign = Paint.Align.RIGHT
        })
        y += 14f
        canvas.drawText("Brutto:", margin, y, paintBold)
        canvas.drawText(currencyFormat.format(calculation.totalGross), margin + contentWidth, y, paintBold)
        paintBold.textAlign = Paint.Align.LEFT
        y += 20f

        if (calculation.warnings.isNotEmpty()) {
            drawLine()
            canvas.drawText("Ostrzeżenia:", margin, y, paintBold)
            y += 12f
            calculation.warnings.forEach { w ->
                checkSpace(12f)
                canvas.drawText("  • $w", margin, y, paintSmall)
                y += 11f
            }
        }

        // Footer
        val footerText = "Wygenerowano: ${dateFormat.format(Date())}   |   Strona $pageIndex"
        canvas.drawText(footerText, margin, (pageHeight - 20).toFloat(), paintSmall)

        document.finishPage(page)

        val file = File(context.cacheDir, "wycena_${quote.id}_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        file
    }
}
