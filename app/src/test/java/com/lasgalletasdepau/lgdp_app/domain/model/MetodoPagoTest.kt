package com.lasgalletasdepau.lgdp_app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetodoPagoTest {

    @Test
    fun `fromString should return correct MetodoPago for valid names`() {
        assertEquals(MetodoPago.EFECTIVO, MetodoPago.fromString("EFECTIVO"))
        assertEquals(MetodoPago.BILLETERA_DIGITAL, MetodoPago.fromString("BILLETERA_DIGITAL"))
        assertEquals(MetodoPago.IZIPAY, MetodoPago.fromString("IZIPAY"))
    }

    @Test
    fun `fromString should handle legacy YAPE as BILLETERA_DIGITAL`() {
        assertEquals(MetodoPago.BILLETERA_DIGITAL, MetodoPago.fromString("YAPE"))
    }

    @Test
    fun `fromString should return null for invalid or null names`() {
        assertNull(MetodoPago.fromString(null))
        assertNull(MetodoPago.fromString(""))
        assertNull(MetodoPago.fromString("INVALID"))
    }
}
