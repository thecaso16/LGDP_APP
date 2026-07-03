package com.lasgalletasdepau.lgdp_app.data.local.dao

import androidx.room.*
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- MESAS ---
    @Query("SELECT * FROM mesas")
    fun obtenerEstadoMesas(): Flow<List<MesaEntity>>

    @Query("UPDATE mesas SET estado = :nuevoEstado WHERE id = :mesaId")
    suspend fun actualizarEstadoMesa(mesaId: Int, nuevoEstado: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inicializarMesas(mesas: List<MesaEntity>)

    // --- PRODUCTOS ---
    @Query("SELECT * FROM productos")
    fun obtenerProductos(): Flow<List<ProductoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProductos(productos: List<ProductoEntity>)

    // --- PEDIDOS (Offline-First) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPedido(pedido: PedidoEntity)

    @Update
    suspend fun actualizarPedido(pedido: PedidoEntity)

    @Query("SELECT * FROM pedidos WHERE sincronizado = 0")
    suspend fun obtenerPedidosPendientesDeSincronizar(): List<PedidoEntity>

    @Query("UPDATE pedidos SET sincronizado = 1 WHERE pedidoId = :pedidoId")
    suspend fun marcarPedidoComoSincronizado(pedidoId: String)

    // --- DETALLES DE PEDIDO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDetallesPedido(detalles: List<PedidoDetalleEntity>)

    @Query("DELETE FROM pedido_detalles WHERE pedidoId = :pedidoId")
    suspend fun eliminarDetallesPorPedido(pedidoId: String)

    // --- REPORTES Y CUADRE ---
    @Query("SELECT SUM(total) FROM pedidos WHERE estado = 'Pagado' AND metodoPago = :metodo AND fecha >= :inicioTurno")
    suspend fun obtenerIngresosPorMetodoPago(metodo: String, inicioTurno: Long): Double?

}