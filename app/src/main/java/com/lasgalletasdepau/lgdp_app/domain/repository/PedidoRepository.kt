package com.lasgalletasdepau.lgdp_app.domain.repository

import com.lasgalletasdepau.lgdp_app.domain.model.CajaDetalle
import com.lasgalletasdepau.lgdp_app.domain.model.CajaSesion
import com.lasgalletasdepau.lgdp_app.domain.model.Mesa
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.Pedido
import com.lasgalletasdepau.lgdp_app.domain.model.PedidoConDetalles
import com.lasgalletasdepau.lgdp_app.domain.model.TrabajadorEstadistica
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    // Mesas
    fun obtenerEstadoMesas(): Flow<List<Mesa>>
    suspend fun marcarMesaOcupada(mesaId: Int, cliente: String)
    suspend fun liberarMesa(mesaId: Int)

    // Pedidos
    suspend fun crearPedido(pedido: Pedido)
    suspend fun actualizarPedido(pedido: Pedido)
    suspend fun obtenerPedidoActivoPorMesa(mesaId: Int): Pedido?
    suspend fun obtenerPedidoPorId(pedidoId: String): Pedido?
    suspend fun obtenerUltimoNumeroPedidoDelDia(): Int
    suspend fun obtenerPedidosActivos(usuarioId: String, verTodo: Boolean): List<Pedido>
    suspend fun obtenerPedidosHistorial(usuarioId: String, inicio: Long, fin: Long, verTodo: Boolean): List<Pedido>
    suspend fun finalizarVenta(pedidoId: String, metodo: MetodoPago, mesaId: Int?)
    suspend fun anularPedido(pedidoId: String, justificacion: String)
    suspend fun bajarHistorialRango(usuarioId: String, inicio: Long, fin: Long)

    // Estadísticas
    suspend fun obtenerEstadisticasTrabajadores(inicio: Long, fin: Long): List<TrabajadorEstadistica>
    suspend fun obtenerPedidosConDetallesPorTrabajador(usuarioId: String, inicio: Long, fin: Long): List<PedidoConDetalles>
    suspend fun obtenerReporteNegocio(inicio: Long, fin: Long): Map<String, Any>

    // Caja
    fun obtenerCajaAbierta(): Flow<CajaSesion?>
    suspend fun abrirCaja(sesion: CajaSesion)
    suspend fun cerrarCaja(sesion: CajaSesion, detalle: CajaDetalle): Boolean
    fun observarIngresosCaja(metodo: MetodoPago, inicioTurno: Long, finTurno: Long): Flow<Double?>
    
    // Sync
    suspend fun sincronizarPedidosYEstado()
}
