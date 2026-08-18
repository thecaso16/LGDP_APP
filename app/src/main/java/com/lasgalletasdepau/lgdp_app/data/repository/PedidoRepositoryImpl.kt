package com.lasgalletasdepau.lgdp_app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.data.mapper.*
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.remote.*
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*

class PedidoRepositoryImpl(
    private val appDao: AppDao,
    private val syncManager: SyncManager,
    private val usuarioRepository: UsuarioRepository,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PedidoRepository {

    override fun obtenerEstadoMesas(): Flow<List<Mesa>> {
        return appDao.obtenerEstadoMesas().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun marcarMesaOcupada(mesaId: Int, cliente: String) {
        appDao.marcarMesaOcupada(mesaId, cliente)
    }

    override suspend fun liberarMesa(mesaId: Int) {
        appDao.liberarMesa(mesaId)
    }

    override suspend fun crearPedido(pedido: Pedido) {
        appDao.insertarPedido(pedido.toEntity())
        appDao.insertarDetallesPedido(pedido.detalles.map { it.toEntity(pedido.pedidoId) })
    }

    override suspend fun actualizarPedido(pedido: Pedido) {
        appDao.actualizarPedido(pedido.toEntity())
        appDao.eliminarDetallesPorPedido(pedido.pedidoId)
        appDao.insertarDetallesPedido(pedido.detalles.map { it.toEntity(pedido.pedidoId) })
    }

    override suspend fun obtenerPedidoActivoPorMesa(mesaId: Int): Pedido? {
        val entity = appDao.obtenerPedidoActivoPorMesa(mesaId) ?: return null
        val detalles = appDao.obtenerDetallesPorPedido(entity.pedidoId)
        return entity.toDomain(detalles)
    }

    override suspend fun obtenerPedidoPorId(pedidoId: String): Pedido? {
        val entity = appDao.obtenerPedidoPorId(pedidoId) ?: return null
        val detalles = appDao.obtenerDetallesPorPedido(pedidoId)
        return entity.toDomain(detalles)
    }

    override suspend fun obtenerUltimoNumeroPedidoDelDia(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val inicio = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val fin = calendar.timeInMillis
        return appDao.obtenerUltimoNumeroPedidoDelDia(inicio, fin) ?: 0
    }

    override suspend fun obtenerPedidosActivos(usuarioId: String, verTodo: Boolean): List<Pedido> {
        val entities = appDao.obtenerPedidosActivos(usuarioId, if (verTodo) 1 else 0)
        return entities.map { entity ->
            val detalles = appDao.obtenerDetallesPorPedido(entity.pedidoId)
            entity.toDomain(detalles)
        }
    }

    override suspend fun obtenerPedidosHistorial(usuarioId: String, inicio: Long, fin: Long, verTodo: Boolean): List<Pedido> {
        val entities = appDao.obtenerPedidosHistorial(usuarioId, inicio, fin, if (verTodo) 1 else 0)
        return entities.map { entity ->
            val detalles = appDao.obtenerDetallesPorPedido(entity.pedidoId)
            entity.toDomain(detalles)
        }
    }

    override suspend fun finalizarVenta(pedidoId: String, metodo: MetodoPago, mesaId: Int?) {
        appDao.finalizarVenta(pedidoId, metodo, mesaId)
    }

    override suspend fun anularPedido(pedidoId: String, justificacion: String) {
        appDao.anularPedido(pedidoId, justificacion)
    }

    override suspend fun bajarHistorialRango(usuarioId: String, inicio: Long, fin: Long) {
        syncManager.bajarHistorialRango(usuarioId, inicio, fin)
    }

    override fun obtenerCajaAbierta(): Flow<CajaSesion?> {
        return appDao.obtenerCajaAbierta().map { it?.toDomain() }
    }

    override suspend fun abrirCaja(sesion: CajaSesion) {
        val data = hashMapOf(
            "cajaId" to sesion.cajaId,
            "usuarioCajeroId" to sesion.usuarioCajeroId,
            "usuarioCajeroNombre" to sesion.nombreCajero,
            "fechaApertura" to Timestamp(Date(sesion.fechaApertura)),
            "montoApertura" to sesion.montoApertura,
            "estado" to "ABIERTA"
        )
        try {
            firestore.collection("cierres_caja").document(sesion.cajaId).set(data).await()
            appDao.abrirCajaLocal(sesion.toEntity())
        } catch (e: Exception) {
            // Si falla remoto, igual abrimos local para modo offline
            appDao.abrirCajaLocal(sesion.toEntity())
        }
    }

    override suspend fun cerrarCaja(sesion: CajaSesion, detalle: CajaDetalle): Boolean {
        val cierreData = hashMapOf(
            "cajaId" to sesion.cajaId,
            "diferencia" to detalle.diferencia,
            "egresos" to detalle.egresos,
            "esperadoFisico" to detalle.esperadoFisico,
            "estado" to "CERRADA",
            "fecha" to Timestamp(Date(detalle.fechaCierre)),
            "fechaApertura" to Timestamp(Date(sesion.fechaApertura)),
            "fechaCierre" to Timestamp(Date(detalle.fechaCierre)),
            "ingresosEfectivo" to detalle.ingresosEfectivo,
            "ingresosIzipay" to detalle.ingresosIzipay,
            "ingresosBilleteraDigital" to detalle.ingresosBilleteraDigital,
            "justificacion" to (detalle.justificacion ?: ""),
            "montoApertura" to sesion.montoApertura,
            "montoFisicoReal" to detalle.montoFisicoReal,
            "totalVentas" to detalle.totalVentas,
            "usuarioCajeroId" to sesion.usuarioCajeroId,
            "usuarioCajeroNombre" to sesion.nombreCajero,
            "resultadoBalance" to if (Math.abs(detalle.diferencia) < 0.01) "CUADRADO" else "DESCUADRADO"
        )

        return try {
            // 1. Guardar localmente siempre primero
            appDao.insertarCajaDetalle(detalle.toEntity())
            appDao.actualizarCajaLocal(sesion.toEntity().copy(estado = "CERRADA", sincronizado = false))
            
            // 2. Intentar subir a Firestore
            try {
                firestore.collection("cierres_caja").document(sesion.cajaId).set(cierreData).await()
                // Si llegamos aquí, se sincronizó con éxito
                appDao.actualizarCajaLocal(sesion.toEntity().copy(estado = "CERRADA", sincronizado = true))
                appDao.limpiarSesionesLocales()
                appDao.limpiarDetallesLocales()
                android.util.Log.d("PedidoRepo", "Cierre de caja sincronizado correctamente con Firestore.")
            } catch (e: Exception) {
                // Error de red o Firestore: no bloqueamos el flujo del usuario
                android.util.Log.e("PedidoRepo", "Cierre guardado localmente, pero falló sincronización remota: ${e.message}")
            }
            
            true // Retornamos éxito porque localmente se procesó
        } catch (e: Exception) {
            android.util.Log.e("PedidoRepo", "Error crítico al cerrar caja: ${e.message}")
            false
        }
    }

    override fun observarIngresosCaja(metodo: MetodoPago, inicioTurno: Long, finTurno: Long): Flow<Double?> {
        return appDao.observarIngresosCaja(metodo.name, inicioTurno, finTurno)
    }

    override suspend fun sincronizarPedidosYEstado() {
        syncManager.sincronizarPedidosYEstado()
    }

    override suspend fun obtenerEstadisticasTrabajadores(inicio: Long, fin: Long): List<TrabajadorEstadistica> {
        val calInicio = Calendar.getInstance().apply {
            timeInMillis = inicio
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val calFin = Calendar.getInstance().apply {
            timeInMillis = fin
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }

        val snapshot = firestore.collection("pedidos")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
            .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
            .get().await()

        val mapaTrabajadores = mutableMapOf<String, Pair<Double, Int>>()
        var totalGlobal = 0.0

        for (doc in snapshot.documents) {
            val pedido = doc.toObject(PedidoFirestore::class.java) ?: continue
            if (pedido.estado != "PAGADO") continue

            val uid = pedido.usuarioId ?: "desconocido"
            val total = pedido.total
            totalGlobal += total
                
            val actual = mapaTrabajadores[uid] ?: Pair(0.0, 0)
            mapaTrabajadores[uid] = Pair(actual.first + total, actual.second + 1)
        }

        val listaResult = mutableListOf<TrabajadorEstadistica>()
        for ((uid, stats) in mapaTrabajadores) {
            val nombre = usuarioRepository.obtenerNombreUsuario(uid)
            
            listaResult.add(TrabajadorEstadistica(
                uid, nombre, stats.first, stats.second,
                if (totalGlobal > 0) (stats.first / totalGlobal).toFloat() else 0f
            ))
        }

        return listaResult.sortedByDescending { it.totalVendido }
    }

    override suspend fun obtenerPedidosConDetallesPorTrabajador(usuarioId: String, inicio: Long, fin: Long): List<PedidoConDetalles> {
        val calInicio = Calendar.getInstance().apply {
            timeInMillis = inicio
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val calFin = Calendar.getInstance().apply {
            timeInMillis = fin
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }

        val snapshot = firestore.collection("pedidos")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
            .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
            .get().await()

        return snapshot.documents
            .mapNotNull { it.toObject(PedidoFirestore::class.java) }
            .filter { it.usuarioId == usuarioId && it.estado == "PAGADO" }
            .map { pedidoFirestore ->
                val pedido = pedidoFirestore.toDomain()
                PedidoConDetalles(pedido, pedido.detalles)
            }.sortedByDescending { it.pedido.fecha }
    }

    override suspend fun obtenerReporteNegocio(inicio: Long, fin: Long): Map<String, Any> {
        val calInicio = Calendar.getInstance().apply {
            timeInMillis = inicio
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val calFin = Calendar.getInstance().apply {
            timeInMillis = fin
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }

        // 1. Obtener todos los productos del catálogo
        val snapshotCatalogo = firestore.collection("productos").get().await()
        val todosLosProductos = snapshotCatalogo.documents.associate { 
            it.id to (it.getString("nombre") ?: "Desconocido")
        }

        // 2. Obtener pedidos
        val snapshotPedidos = firestore.collection("pedidos")
            .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
            .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
            .get().await()

        var ingresos = 0.0
        var pedidosCont = 0
        var canceladosCont = 0
        val conteoProductos = mutableMapOf<String, Int>()
        val metodosMap = mutableMapOf<String, Double>()

        todosLosProductos.values.forEach { nombre -> conteoProductos[nombre] = 0 }

        for (doc in snapshotPedidos.documents) {
            val pedido = doc.toObject(PedidoFirestore::class.java) ?: continue
            if (pedido.estado == "PAGADO") {
                val total = pedido.total
                ingresos += total
                pedidosCont++
                val metodoRaw = pedido.metodoPago ?: "EFECTIVO"
                val metodoEnum = MetodoPago.fromString(metodoRaw)
                val metodoNombre = metodoEnum?.valor ?: metodoRaw
                metodosMap[metodoNombre] = (metodosMap[metodoNombre] ?: 0.0) + total
                
                pedido.detalles.forEach { det ->
                    val nombre = det.nombreProducto ?: "Desconocido"
                    conteoProductos[nombre] = (conteoProductos[nombre] ?: 0) + det.cantidad
                }
            } else if (pedido.estado == "CANCELADO") {
                canceladosCont++
            }
        }

        // 3. Obtener egresos
        val snapshotCierres = firestore.collection("cierres_caja")
            .whereGreaterThanOrEqualTo("fechaCierre", Timestamp(calInicio.time))
            .whereLessThanOrEqualTo("fechaCierre", Timestamp(calFin.time))
            .get().await()
        var egresosSum = 0.0
        for (doc in snapshotCierres.documents) { egresosSum += doc.getDouble("egresos") ?: 0.0 }

        return mapOf(
            "totalIngresos" to ingresos,
            "totalPedidos" to pedidosCont,
            "totalCancelados" to canceladosCont,
            "totalEgresos" to egresosSum,
            "conteoProductos" to conteoProductos,
            "ventasPorMetodo" to metodosMap
        )
    }
}
