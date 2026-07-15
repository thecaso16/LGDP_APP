package com.lasgalletasdepau.lgdp_app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.text.SimpleDateFormat
import java.util.*

class PdfReportGenerator(private val context: Context) {

    private val pdfDocument = PdfDocument()
    private var currentPage: PdfDocument.Page? = null
    private var canvas: Canvas? = null
    private var pageNumber = 0
    private var currentY = 0f
    private var reportTitle: String = ""

    private val pageWidth = 595 // A4 width in points
    private val pageHeight = 842 // A4 height in points
    private val margin = 50f
    
    private val paintNormal = Paint().apply {
        textSize = 10f
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    
    private val paintBold = Paint().apply {
        textSize = 10f
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintTitle = Paint().apply {
        textSize = 18f
        color = Color.rgb(30, 35, 61) // Dark blue from UI
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintSubtitle = Paint().apply {
        textSize = 12f
        color = Color.rgb(100, 116, 139)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val paintFooter = Paint().apply {
        textSize = 8f
        color = Color.GRAY
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    }

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    fun startNewPage(title: String) {
        if (currentPage != null) {
            pdfDocument.finishPage(currentPage)
        }
        if (reportTitle.isEmpty()) reportTitle = title
        pageNumber++
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage?.canvas
        currentY = margin

        drawHeader(reportTitle, pageNumber == 1)
    }

    private fun drawHeader(title: String, isFirstPage: Boolean) {
        canvas?.let { c ->
            // App Name (Header consistent across pages)
            paintBold.textSize = 11f
            paintBold.color = Color.rgb(30, 35, 61)
            c.drawText("LAS GALLETAS DE PAU", margin, currentY, paintBold)
            
            if (isFirstPage) {
                currentY += 35f
                // Report Title ONLY on the first page
                paintTitle.textSize = 16f
                c.drawText(title.uppercase(), pageWidth / 2f, currentY, paintTitle.apply { textAlign = Paint.Align.CENTER })
                paintTitle.textAlign = Paint.Align.LEFT // reset
                currentY += 10f
            } else {
                currentY += 10f
            }
            
            // Decorative subtle line
            val paintLine = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.8f
            }
            c.drawLine(margin, currentY, pageWidth - margin, currentY, paintLine)
            currentY += 30f
        }
    }

    private fun drawFooter() {
        canvas?.let { c ->
            val footerY = pageHeight - 30f
            val paintLine = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 0.5f
            }
            c.drawLine(margin, footerY - 10, pageWidth - margin, footerY - 10, paintLine)
            
            val timestamp = "Generado el: ${dateFormat.format(Date())}"
            c.drawText(timestamp, margin, footerY, paintFooter)
            
            val pageInfo = "Página $pageNumber"
            c.drawText(pageInfo, pageWidth - margin - paintFooter.measureText(pageInfo), footerY, paintFooter)
        }
    }

    fun addText(text: String, isBold: Boolean = false, fontSize: Float = 10f, color: Int = Color.BLACK, spaceAfter: Float = 15f) {
        checkPageSpace(spaceAfter)
        val p = if (isBold) paintBold else paintNormal
        p.textSize = fontSize
        p.color = color
        canvas?.drawText(text, margin, currentY, p)
        currentY += spaceAfter
    }

    fun addLabeledText(label: String, value: String, spaceAfter: Float = 15f) {
        checkPageSpace(spaceAfter)
        canvas?.drawText("$label ", margin, currentY, paintBold)
        val labelWidth = paintBold.measureText("$label ")
        canvas?.drawText(value, margin + labelWidth, currentY, paintNormal)
        currentY += spaceAfter
    }

    fun addSectionTitle(title: String) {
        checkPageSpace(40f)
        currentY += 10f
        canvas?.drawText(title, margin, currentY, paintSubtitle)
        currentY += 20f
    }

    fun addHorizontalLine() {
        checkPageSpace(20f)
        val paintLine = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
        }
        canvas?.drawLine(margin, currentY, pageWidth - margin, currentY, paintLine)
        currentY += 20f
    }

    fun addRow(cols: List<String>, weights: List<Float>, isHeader: Boolean = false) {
        val totalWeight = weights.sum()
        val availableWidth = pageWidth - (2 * margin)
        checkPageSpace(20f)

        if (isHeader) {
            val bgPaint = Paint().apply { color = Color.rgb(248, 250, 252) }
            canvas?.drawRect(margin, currentY - 15f, pageWidth - margin, currentY + 5f, bgPaint)
        }

        var currentX = margin
        cols.forEachIndexed { i, text ->
            val colWidth = (weights[i] / totalWeight) * availableWidth
            val p = if (isHeader) paintBold else paintNormal
            
            // Handle text alignment (last column right-aligned)
            if (i == cols.size - 1) {
                p.textAlign = Paint.Align.RIGHT
                canvas?.drawText(text, currentX + colWidth, currentY, p)
                p.textAlign = Paint.Align.LEFT
            } else {
                canvas?.drawText(text, currentX, currentY, p)
            }
            
            currentX += colWidth
        }
        currentY += 20f
    }

    private fun checkPageSpace(needed: Float) {
        if (currentY + needed > pageHeight - margin - 40f) {
            drawFooter()
            startNewPage(reportTitle)
        }
    }

    fun finish(): PdfDocument {
        drawFooter()
        if (currentPage != null) {
            pdfDocument.finishPage(currentPage)
        }
        return pdfDocument
    }
}
