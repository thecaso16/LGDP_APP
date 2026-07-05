package com.lasgalletasdepau.lgdp_app.data.local.dao

import androidx.room.*
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- MESAS ---
    @Query("SELECT * FROM mesas")
    fun obtenerEstadoMesas(): Flow<List<MesaEntity>>

    @Query("UPDATE mesas SET estado = :nuevoEstado, sincronizado = 0 WHERE id = :mesaId")
    suspend fun actualizarEstadoMesa(mesaId: Int, nuevoEstado: String)

    @Query("UPDATE mesas SET estado = 'OCUPADA', clienteActivo = :cliente, sincronizado = 0 WHERE id = :mesaId")
    suspend fun marcarMesaOcupada(mesaId: Int, cliente: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inicializarMesas(mesas: List<MesaEntity>)

    @Query("UPDATE mesas SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarMesaComoSincronizada(id: Int)

    @Query("SELECT * FROM mesas WHERE sincronizado = 0")
    suspend fun obtenerMesasNoSincronizadas(): List<MesaEntity>

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

    @Query("SELECT * FROM pedidos WHERE (usuarioId = :usuarioId OR :verTodo = 1) AND fecha >= :inicio AND fecha <= :fin ORDER BY fecha DESC")
    suspend fun obtenerPedidosHistorial(usuarioId: String, inicio: Long, fin: Long, verTodo: Int): List<PedidoEntity>

    @Query("UPDATE mesas SET estado = 'LIBRE', clienteActivo = NULL, sincronizado = 0 WHERE id = :mesaId")
    suspend fun liberarMesa(mesaId: Int)

    @Query("UPDATE pedidos SET estado = :nuevoEstado, metodoPago = :metodo, sincronizado = 0 WHERE pedidoId = :pedidoId")
    suspend fun actualizarEstadoPedido(pedidoId: String, nuevoEstado: String, metodo: com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago?)

    @Query("UPDATE pedidos SET estado = 'CANCELADO', sincronizado = 0 WHERE pedidoId = :pedidoId")
    suspend fun anularPedido(pedidoId: String)

    @Transaction
    suspend fun finalizarVenta(pedidoId: String, metodo: com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago, mesaId: Int?) {
        actualizarEstadoPedido(pedidoId, "PAGADO", metodo)
        val detalles = obtenerDetallesPorPedido(pedidoId)
        detalles.forEach { det ->
            det.productoId?.let { pid -> descontarStock(pid, det.cantidad) }
        }
        mesaId?.let { liberarMesa(it) }
    }

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
    @Query("SELECT SUM(total) FROM pedidos WHERE estado = 'PAGADO' AND metodoPago = :metodo AND fecha >= :inicioTurno AND fecha <= :finTurno")
    fun observarIngresosCaja(metodo: String, inicioTurno: Long, finTurno: Long): Flow<Double?>

    // --- USUARIOS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioLogueado(): UsuarioEntity?

    @Query("DELETE FROM usuarios")
    suspend fun cerrarSesionLocal()

    // --- SESIONES DE CAJA ---
    @Query("SELECT * FROM caja_sesiones WHERE fechaCierre IS NULL LIMIT 1")
    fun obtenerCajaAbierta(): Flow<CajaSesionEntity?>

    @Query("SELECT * FROM caja_sesiones WHERE fechaCierre IS NULL LIMIT 1")
    suspend fun obtenerCajaAbiertaSync(): CajaSesionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun abrirCajaLocal(sesion: CajaSesionEntity)

    @Update
    suspend fun actualizarCajaLocal(sesion: CajaSesionEntity)
}