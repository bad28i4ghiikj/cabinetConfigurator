package com.company.cabinetConfigurator.domain.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
    private val currencyFmt = NumberFormat.getCurrencyInstance(locale)
    private val dateFmt = SimpleDateFormat("dd.MM.yyyy", locale)

    // Colors
    private val colorPrimary = Color.rgb(21, 101, 192)     // dark blue
    private val colorPrimaryLight = Color.rgb(227, 242, 253) // light blue
    private val colorRowAlt = Color.rgb(250, 250, 250)
    private val colorBorder = Color.rgb(189, 189, 189)
    private val colorTextMain = Color.rgb(33, 33, 33)
    private val colorTextSub = Color.rgb(97, 97, 97)
    private val colorWhite = Color.WHITE
    private val colorTotalBg = Color.rgb(21, 101, 192)

    private val PAGE_W = 595
    private val PAGE_H = 842
    private val MARGIN = 36f
    private val CONTENT_W get() = PAGE_W - 2 * MARGIN

    // Paints
    private fun paint(size: Float, color: Int = colorTextMain, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textSize = size
            this.color = color
            textAlign = align
        }

    private fun fillPaint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }

    private fun strokePaint(color: Int, width: Float = 0.5f) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = color
        strokeWidth = width
    }

    suspend fun generateQuotePdf(
        quote: Quote,
        calculation: QuoteCalculationResult,
        logoUri: Uri?,
        companyName: String?
    ): File = withContext(Dispatchers.IO) {

        val logoBitmap = logoUri?.let { loadBitmap(it, 160, 64) }

        val document = PdfDocument()
        val state = PageState(document)

        drawHeader(state, quote, logoBitmap, companyName)
        drawFurnitureSection(state, quote)
        drawCalculationTable(state, calculation)
        drawTotals(state, calculation)
        if (calculation.warnings.isNotEmpty()) drawWarnings(state, calculation.warnings)

        state.finishPage()

        val file = File(context.cacheDir, "wycena_${quote.id}_${System.currentTimeMillis()}.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()
        file
    }

    // ── Page state helper ─────────────────────────────────────────────────────

    inner class PageState(val document: PdfDocument) {
        private var pageIndex = 0
        private var page: PdfDocument.Page? = null
        var canvas: Canvas = Canvas()
        var y = MARGIN

        init { startPage() }

        private fun startPage() {
            pageIndex++
            val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageIndex).create()
            page = document.startPage(info)
            canvas = page!!.canvas
            y = MARGIN
        }

        fun finishPage() {
            drawFooter(pageIndex)
            page?.let { document.finishPage(it) }
            page = null
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_H - MARGIN - 24f) {
                finishPage()
                startPage()
            }
        }

        fun advance(dy: Float) { y += dy }

        private fun drawFooter(index: Int) {
            val p = paint(7.5f, colorTextSub)
            canvas.drawText(
                "Wygenerowano: ${dateFmt.format(Date())}",
                MARGIN, (PAGE_H - 14).toFloat(), p
            )
            canvas.drawText(
                "Strona $index",
                PAGE_W - MARGIN, (PAGE_H - 14).toFloat(),
                p.also { it.textAlign = Paint.Align.RIGHT }
            )
            canvas.drawLine(MARGIN, (PAGE_H - 22).toFloat(), PAGE_W - MARGIN, (PAGE_H - 22).toFloat(), strokePaint(colorBorder))
        }
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private fun drawHeader(state: PageState, quote: Quote, logo: Bitmap?, companyName: String?) {
        val c = state.canvas
        val headerH = if (logo != null || companyName != null) 80f else 56f

        // Blue background bar
        c.drawRect(RectF(0f, 0f, PAGE_W.toFloat(), headerH), fillPaint(colorPrimary))

        var leftX = MARGIN
        var logoBottomY = 0f

        // Logo
        if (logo != null) {
            val logoTop = (headerH - logo.height) / 2f
            c.drawBitmap(logo, leftX, logoTop, null)
            leftX += logo.width + 12f
            logoBottomY = logoTop + logo.height
        }

        // Company name
        companyName?.let {
            val pCompany = paint(13f, colorWhite, bold = true)
            c.drawText(it, leftX, headerH / 2f - 4f, pCompany)
        }

        // Quote title (right-aligned)
        val quoteTitle = quote.name.ifBlank { "Wycena #${quote.id}" }
        c.drawText(
            quoteTitle,
            PAGE_W - MARGIN, headerH / 2f - 6f,
            paint(14f, colorWhite, bold = true, align = Paint.Align.RIGHT)
        )
        c.drawText(
            dateFmt.format(Date(quote.createdAt)),
            PAGE_W - MARGIN, headerH / 2f + 12f,
            paint(9f, Color.rgb(187, 222, 251), align = Paint.Align.RIGHT)
        )

        state.y = headerH + 14f
    }

    // ── Section header ────────────────────────────────────────────────────────

    private fun drawSectionHeader(state: PageState, title: String) {
        state.ensureSpace(24f)
        val c = state.canvas
        c.drawRect(RectF(MARGIN, state.y, MARGIN + CONTENT_W, state.y + 18f), fillPaint(colorPrimaryLight))
        c.drawRect(RectF(MARGIN, state.y, MARGIN + 3f, state.y + 18f), fillPaint(colorPrimary))
        c.drawText(title, MARGIN + 8f, state.y + 13f, paint(9f, colorPrimary, bold = true))
        state.advance(22f)
    }

    // ── Furniture section ─────────────────────────────────────────────────────

    private fun drawFurnitureSection(state: PageState, quote: Quote) {
        if (quote.furniture.isEmpty()) return

        drawSectionHeader(state, "ZESTAWIENIE MEBLI")

        quote.furniture.forEachIndexed { idx, piece ->
            state.ensureSpace(16f)
            val pieceName = piece.name.ifBlank { "Mebel ${idx + 1}" }
            val label = "${idx + 1}. $pieceName  —  ${piece.cabinetType.displayName}   ${piece.widthMm} × ${piece.heightMm} × ${piece.depthMm} mm"

            // Row background
            if (idx % 2 == 0) {
                state.canvas.drawRect(RectF(MARGIN, state.y - 2f, MARGIN + CONTENT_W, state.y + 12f), fillPaint(colorRowAlt))
            }
            state.canvas.drawText(label, MARGIN + 6f, state.y + 10f, paint(9.5f, colorTextMain, bold = true))
            state.advance(13f)

            piece.elements.forEach { elem ->
                state.ensureSpace(12f)
                val accStr = if (elem.accessories.isNotEmpty())
                    "  [${elem.accessories.joinToString(", ") { "${it.manufacturer} ${it.model}".trim().ifBlank { it.type.displayName } }}]"
                else ""
                state.canvas.drawText(
                    "     • ${elem.type.displayName} ×${elem.quantity}$accStr",
                    MARGIN + 6f, state.y + 9f,
                    paint(8.5f, colorTextSub)
                )
                state.advance(12f)
            }
            state.advance(3f)
        }
        state.advance(8f)
    }

    // ── Calculation table ─────────────────────────────────────────────────────

    private val COL_DESC = MARGIN
    private val COL_QTY get() = MARGIN + CONTENT_W * 0.50f
    private val COL_UNIT_PRICE get() = MARGIN + CONTENT_W * 0.70f
    private val COL_TOTAL get() = MARGIN + CONTENT_W

    private fun drawCalculationTable(state: PageState, calc: QuoteCalculationResult) {
        drawSectionHeader(state, "KALKULACJA")

        // Table header row
        state.ensureSpace(18f)
        val c = state.canvas
        c.drawRect(RectF(MARGIN, state.y, MARGIN + CONTENT_W, state.y + 16f), fillPaint(colorPrimary))
        val hPaint = paint(8.5f, colorWhite, bold = true)
        c.drawText("Pozycja", COL_DESC + 4f, state.y + 11f, hPaint)
        c.drawText("Ilość / Jedn.", COL_QTY + 4f, state.y + 11f, hPaint)
        c.drawText("Cena jedn.", COL_UNIT_PRICE + 4f, state.y + 11f, hPaint)
        c.drawText("Kwota", COL_TOTAL - 4f, state.y + 11f, hPaint.also { it.textAlign = Paint.Align.RIGHT })
        state.advance(17f)

        calc.lines.forEachIndexed { i, line ->
            state.ensureSpace(14f)
            val rowBg = if (i % 2 == 0) colorWhite else colorRowAlt
            state.canvas.drawRect(RectF(MARGIN, state.y - 1f, MARGIN + CONTENT_W, state.y + 13f), fillPaint(rowBg))

            val qtyStr = if (line.quantity != null) "${fmtNum(line.quantity)} ${line.unit.orEmpty()}" else "—"
            val priceStr = if (line.unitPrice != null) currencyFmt.format(line.unitPrice) else "—"

            state.canvas.drawText(line.label, COL_DESC + 4f, state.y + 10f, paint(9f, colorTextMain))
            state.canvas.drawText(qtyStr, COL_QTY + 4f, state.y + 10f, paint(9f, colorTextSub))
            state.canvas.drawText(priceStr, COL_UNIT_PRICE + 4f, state.y + 10f, paint(9f, colorTextSub))
            state.canvas.drawText(
                currencyFmt.format(line.amount),
                COL_TOTAL - 4f, state.y + 10f,
                paint(9f, colorTextMain, align = Paint.Align.RIGHT)
            )

            // Row border
            state.canvas.drawLine(MARGIN, state.y + 13f, MARGIN + CONTENT_W, state.y + 13f, strokePaint(colorBorder, 0.3f))
            state.advance(14f)
        }

        // Outer border around table
        state.canvas.drawRect(
            RectF(MARGIN, state.y - calc.lines.size * 14f - 17f, MARGIN + CONTENT_W, state.y),
            strokePaint(colorBorder, 0.8f)
        )

        state.advance(10f)
    }

    // ── Totals ────────────────────────────────────────────────────────────────

    private fun drawTotals(state: PageState, calc: QuoteCalculationResult) {
        state.ensureSpace(72f)
        val c = state.canvas
        val boxLeft = MARGIN + CONTENT_W * 0.50f
        val boxWidth = CONTENT_W * 0.50f
        var ty = state.y

        // Net row
        c.drawRect(RectF(boxLeft, ty, boxLeft + boxWidth, ty + 20f), fillPaint(colorRowAlt))
        c.drawRect(RectF(boxLeft, ty, boxLeft + boxWidth, ty + 20f), strokePaint(colorBorder))
        c.drawText("Wartość netto:", boxLeft + 8f, ty + 14f, paint(9.5f, colorTextSub))
        c.drawText(currencyFmt.format(calc.totalNet), boxLeft + boxWidth - 8f, ty + 14f, paint(9.5f, colorTextMain, bold = true, align = Paint.Align.RIGHT))
        ty += 20f

        // VAT row
        val vatAmount = calc.totalGross - calc.totalNet
        c.drawRect(RectF(boxLeft, ty, boxLeft + boxWidth, ty + 20f), fillPaint(colorWhite))
        c.drawRect(RectF(boxLeft, ty, boxLeft + boxWidth, ty + 20f), strokePaint(colorBorder))
        c.drawText("VAT:", boxLeft + 8f, ty + 14f, paint(9.5f, colorTextSub))
        c.drawText(currencyFmt.format(vatAmount), boxLeft + boxWidth - 8f, ty + 14f, paint(9.5f, colorTextSub, align = Paint.Align.RIGHT))
        ty += 20f

        // Gross row (highlighted)
        c.drawRect(RectF(boxLeft, ty, boxLeft + boxWidth, ty + 28f), fillPaint(colorTotalBg))
        c.drawText("Wartość brutto:", boxLeft + 8f, ty + 18f, paint(10.5f, colorWhite))
        c.drawText(currencyFmt.format(calc.totalGross), boxLeft + boxWidth - 8f, ty + 18f, paint(12f, colorWhite, bold = true, align = Paint.Align.RIGHT))
        ty += 28f

        state.y = ty + 14f
    }

    // ── Warnings ──────────────────────────────────────────────────────────────

    private fun drawWarnings(state: PageState, warnings: List<String>) {
        state.ensureSpace(20f + warnings.size * 13f)
        drawSectionHeader(state, "OSTRZEŻENIA")
        warnings.forEach { w ->
            state.canvas.drawText("⚠  $w", MARGIN + 6f, state.y + 10f, paint(8.5f, Color.rgb(198, 40, 40)))
            state.advance(13f)
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun fmtNum(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else String.format(locale, "%.2f", v)

    private suspend fun loadBitmap(uri: Uri, maxW: Int, maxH: Int): Bitmap? =
        runCatching {
            val loader = ImageLoader(context)
            val req = ImageRequest.Builder(context).data(uri).size(maxW, maxH).build()
            val result = loader.execute(req)
            (result as? SuccessResult)?.drawable?.let { d ->
                Bitmap.createBitmap(d.intrinsicWidth.coerceAtLeast(1), d.intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888).also { bmp ->
                    val cv = Canvas(bmp)
                    d.setBounds(0, 0, bmp.width, bmp.height)
                    d.draw(cv)
                }
            }
        }.getOrNull()
}
