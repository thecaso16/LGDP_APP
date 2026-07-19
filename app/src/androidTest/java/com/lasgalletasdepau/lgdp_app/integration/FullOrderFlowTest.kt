package com.lasgalletasdepau.lgdp_app.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullOrderFlowTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.appDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun flow_OcuparMesa_HacerPedido_Pagar_LiberaMesaYDescuentaStock() = runBlocking {
        // 1. Inicializar datos
        val mesaId = 1
        val productoId = "p1"
        
        dao.inicializarMesas(listOf(MesaEntity(mesaId, "Mesa 01", EstadoMesa.LIBRE, null, true)))
        dao.insertarProductos(listOf(ProductoEntity(
            productoId, "Galleta", "", null, 5.0, 10, true, "cat1", false, true, true, true, 0, null
        )))
        
        // 2. Ocupar Mesa
        dao.marcarMesaOcupada(mesaId, "Cliente Test")
        var mesas = dao.obtenerEstadoMesas().first()
        assertEquals(EstadoMesa.OCUPADA, mesas.find { it.id == mesaId }?.estado)
        
        // 3. Crear Pedido
        val pedidoId = "ped_123"
        val pedido = PedidoEntity(
            pedidoId = pedidoId,
            numeroPedido = 1,
            fecha = System.currentTimeMillis(),
            estado = EstadoPedido.PENDIENTE,
            tipoPedido = TipoPedido.EN_MESA,
            mesaId = mesaId,
            metodoPago = null,
            nombreCliente = "Cliente Test",
            total = 5.0,
            usuarioId = "u1",
            usuarioNombre = "Admin",
            notas = null,
            cajaId = "caja1",
            sincronizado = false
        )
        dao.insertarPedido(pedido)
        dao.insertarDetallesPedido(listOf(PedidoDetalleEntity(
            pedidoId = pedidoId,
            productoId = productoId,
            nombreProducto = "Galleta",
            cantidad = 2,
            precioUnitario = 5.0,
            comentario = null
        )))
        
        // 4. Finalizar Venta (Cobrar)
        dao.finalizarVenta(pedidoId, MetodoPago.EFECTIVO, mesaId)
        
        // 5. Verificaciones
        // Mesa debe estar libre
        mesas = dao.obtenerEstadoMesas().first()
        assertEquals(EstadoMesa.LIBRE, mesas.find { it.id == mesaId }?.estado)
        assertNull(mesas.find { it.id == mesaId }?.clienteActivo)
        
        // Stock debe haber bajado de 10 a 8
        val productos = dao.obtenerProductos().first()
        assertEquals(8, productos.find { it.productoId == productoId }?.stock)
        
        // Pedido debe estar PAGADO
        val pedidoGuardado = dao.obtenerPedidoPorId(pedidoId)
        assertEquals("PAGADO", pedidoGuardado?.estado?.name)
    }
}
