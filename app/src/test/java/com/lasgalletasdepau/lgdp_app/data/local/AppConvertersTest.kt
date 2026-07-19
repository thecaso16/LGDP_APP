package com.lasgalletasdepau.lgdp_app.data.local

import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppConvertersTest {

    private val converters = AppConverters()

    @Test
    fun `EstadoMesa conversion`() {
        assertEquals("LIBRE", converters.fromEstadoMesa(EstadoMesa.LIBRE))
        assertEquals(EstadoMesa.OCUPADA, converters.toEstadoMesa("OCUPADA"))
    }

    @Test
    fun `EstadoPedido conversion with nulls`() {
        assertNull(converters.fromEstadoPedido(null))
        assertEquals("PENDIENTE", converters.fromEstadoPedido(EstadoPedido.PENDIENTE))
        
        assertNull(converters.toEstadoPedido(null))
        assertEquals(EstadoPedido.CANCELADO, converters.toEstadoPedido("CANCELADO"))
    }

    @Test
    fun `TipoPedido conversion`() {
        assertEquals("EN_MESA", converters.fromTipoPedido(TipoPedido.EN_MESA))
        assertEquals(TipoPedido.PARA_LLEVAR, converters.toTipoPedido("PARA_LLEVAR"))
    }

    @Test
    fun `MetodoPago conversion with legacy support`() {
        assertEquals("EFECTIVO", converters.fromMetodoPago(MetodoPago.EFECTIVO))
        assertNull(converters.fromMetodoPago(null))

        assertEquals(MetodoPago.BILLETERA_DIGITAL, converters.toMetodoPago("BILLETERA_DIGITAL"))
        assertEquals(MetodoPago.BILLETERA_DIGITAL, converters.toMetodoPago("YAPE"))
        assertNull(converters.toMetodoPago(null))
    }
}
