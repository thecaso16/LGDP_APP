package com.lasgalletasdepau.lgdp_app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class PdfReportGeneratorTest {

    private val context = mockk<Context>(relaxed = true)
    private val mockPage = mockk<PdfDocument.Page>(relaxed = true)
    private val mockCanvas = mockk<Canvas>(relaxed = true)
    private val mockPageInfo = mockk<PdfDocument.PageInfo>(relaxed = true)

    @Before
    fun setup() {
        mockkConstructor(PdfDocument::class)
        mockkConstructor(Paint::class)
        mockkConstructor(PdfDocument.PageInfo.Builder::class)
        mockkStatic(Typeface::class)
        mockkStatic(Color::class)
        
        every { Typeface.create(any<Typeface>(), any()) } returns mockk(relaxed = true)
        every { Typeface.create(any<String>(), any()) } returns mockk(relaxed = true)
        every { Color.rgb(any<Int>(), any(), any()) } returns 0
        
        every { anyConstructed<PdfDocument.PageInfo.Builder>().create() } returns mockPageInfo
        every { anyConstructed<PdfDocument>().startPage(any()) } returns mockPage
        every { anyConstructed<PdfDocument>().finishPage(any()) } just Runs
        every { mockPage.canvas } returns mockCanvas
        
        // Mock all Paint methods used
        every { anyConstructed<Paint>().textSize = any() } just Runs
        every { anyConstructed<Paint>().color = any() } just Runs
        every { anyConstructed<Paint>().setTypeface(any()) } returns null
        every { anyConstructed<Paint>().measureText(any<String>()) } returns 10f
        every { anyConstructed<Paint>().textAlign = any() } just Runs
        every { anyConstructed<Paint>().strokeWidth = any() } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `pdf generation mock test`() {
        val generator = PdfReportGenerator(context)
        generator.startNewPage("Test Title")
        generator.addText("Hello World")
        generator.addRow(listOf("A", "B"), listOf(1f, 1f))
        
        val doc = generator.finish()
        assertNotNull(doc)
    }
}
