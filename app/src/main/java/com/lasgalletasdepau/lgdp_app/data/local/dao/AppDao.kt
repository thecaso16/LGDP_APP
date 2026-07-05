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

    @Query("UPDATE mesas SET estado = 'OCUPADA', clienteActivo = :cliente WHERE id = :mesaId")
    suspend fun marcarMesaOcupada(mesaId: Int, cliente: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inicializarMesas(mesas: List<MesaEntity>)

    // --- CATEGORÍAS ---
    @Query("SELECT * FROM categorias")
    fun obtenerCategorias(): Flow<List<CategoriaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategorias(categorias: List<CategoriaEntity>)

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

    @Query("SELECT * FROM pedidos WHERE mesaId = :mesaId AND estado != 'PAGADO' AND estado != 'CANCELADO' LIMIT 1")
    suspend fun obtenerPedidoActivoPorMesa(mesaId: Int): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE pedidoId = :pedidoId")
    suspend fun obtenerPedidoPorId(pedidoId: String): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE estado != 'PAGADO' AND estado != 'CANCELADO' ORDER BY fecha DESC")
    suspend fun obtenerPedidosActivosGenerales(): List<PedidoEntity>

    @Query("SELECT * FROM pedidos WHERE usuarioId = :usuarioId AND fecha >= :inicio AND fecha <= :fin ORDER BY fecha DESC")
    suspend fun obtenerPedidosPorFechaYUsuario(usuarioId: String, inicio: Long, fin: Long): List<PedidoEntity>

    @Query("UPDATE mesas SET estado = 'LIBRE', clienteActivo = NULL WHERE id = :mesaId")
    suspend fun liberarMesa(mesaId: Int)

    @Transaction
    suspend fun finalizarVenta(pedidoId: String, metodo: String, mesaId: Int?) {
        // 1. Actualizar Pedido
        actualizarEstadoPedido(pedidoId, "PAGADO", metodo)
        
        // 2. Descontar Stock
        val detalles = obtenerDetallesPorPedido(pedidoId)
        detalles.forEach { det ->
            det.productoId?.let { pid ->
                descontarStock(pid, det.cantidad)
            }
        }

        // 3. Liberar Mesa si aplica
        mesaId?.let { liberarMesa(it) }
    }

    @Query("UPDATE pedidos SET estado = :nuevoEstado, metodoPago = :metodo, sincronizado = 0 WHERE pedidoId = :pedidoId")
    suspend fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: String, metodo: String)

    @Query("UPDATE productos SET stock = stock - :cantidad, sincronizado = 0 WHERE productoId = :id")
    suspend fun descontarStock(id: String, cantidad: Int)

    @Query("SELECT * FROM productos WHERE sincronizado = 0")
    suspend fun obtenerProductosNoSincronizados(): List<ProductoEntity>

    @Query("UPDATE productos SET sincronizado = 1 WHERE productoId = :id")
    suspend fun marcarProductoComoSincronizado(id: String)

    // --- DETALLES DE PEDIDO ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDetallesPedido(detalles: List<PedidoDetalleEntity>)

    @Query("SELECT * FROM pedido_detalles WHERE pedidoId = :pedidoId")

    suspend fun obtenerDetallesPorPedido(pedidoId: String): List<PedidoDetalleEntity>
    @Query("DELETE FROM pedido_detalles WHERE pedidoId = :pedidoId")
    suspend fun eliminarDetallesPorPedido(pedidoId: String)

    // --- REPORTES Y CUADRE ---
    @Query("SELECT SUM(total) FROM pedidos WHERE estado = 'PAGADO' AND metodoPago = :metodo AND fecha >= :inicioTurno")
    fun observarIngresosPorMetodoPago(metodo: String, inicioTurno: Long): Flow<Double?>

    @Query("SELECT SUM(total) FROM pedidos WHERE estado = 'PAGADO' AND metodoPago = :metodo AND fecha >= :inicioTurno")
    suspend fun obtenerIngresosPorMetodoPago(metodo: String, inicioTurno: Long): Double?

    // --- USUARIOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioLogueado(): UsuarioEntity?

    @Query("DELETE FROM usuarios")
    suspend fun cerrarSesionLocal()
}