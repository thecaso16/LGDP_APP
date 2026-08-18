package com.lasgalletasdepau.lgdp_app.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.data.mapper.*
import com.lasgalletasdepau.lgdp_app.domain.model.*
import kotlinx.coroutines.tasks.await
import java.util.Date

class SyncManager(
    private val appDao: AppDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    companion object {
        @Volatile
        private var INSTANCE: SyncManager? = null

        fun getInstance(context: android.content.Context): SyncManager {
            return INSTANCE ?: synchronized(this) {
                val appDao = com.lasgalletasdepau.lgdp_app.data.local.AppDatabase.getDatabase(context).appDao()
                val instance = SyncManager(appDao)
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun sincronizarTodo() {
        try {
            Log.d("SyncManager", "Iniciando sincronización bidireccional...")
            subirPedidosPendientes()
            subirActualizacionesDeStock()
            subirEstadoMesas()

            // Estas descargas son pesadas, se podrían hacer con menos frecuencia
            bajarMesasDeFirebase()
            bajarCategoriasDeFirebase()
            bajarProductosDeFirebase()
            bajarInsumosDeFirebase()
            bajarRelacionesInsumosDeFirebase()
            
            // Estas son más críticas para la operación
            bajarCajaAbiertaDeFirebase()
            bajarPedidosActivosDeFirebase()
            Log.d("SyncManager", "Sincronización completada con éxito.")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error en sincronización: ${e.message}")
        }
    }

    /**
     * Sincronización rápida para los elementos críticos de la operación diaria.
     */
    suspend fun sincronizarPedidosYEstado() {
        try {
            subirPedidosPendientes()
            subirEstadoMesas()
            bajarCajaAbiertaDeFirebase()
            bajarPedidosActivosDeFirebase()
            bajarMesasDeFirebase()
        } catch (e: Exception) {
            Log.e("SyncManager", "Error en sincronización rápida: ${e.message}")
        }
    }

    private suspend fun subirActualizacionesDeStock() {
        val productosNoSincro = appDao.obtenerProductosNoSincronizados()
        if (productosNoSincro.isEmpty()) return

        val batch = firestore.batch()
        for (prod in productosNoSincro) {
            val ref = firestore.collection("productos").document(prod.productoId)
            batch.update(ref, "stock", prod.stock)
        }
        batch.commit().await()

        for (prod in productosNoSincro) {
            appDao.marcarProductoComoSincronizado(prod.productoId)
        }
    }

    private suspend fun subirEstadoMesas() {
        val mesasNoSincro = appDao.obtenerMesasNoSincronizadas()
        if (mesasNoSincro.isEmpty()) return

        val batch = firestore.batch()
        for (mesa in mesasNoSincro) {
            val ref = firestore.collection("mesas").document(mesa.id.toString())
            val datos = hashMapOf(
                "id" to mesa.id,
                "estado" to mesa.estado.name,
                "clienteActivo" to mesa.clienteActivo
            )
            batch.set(ref, datos, SetOptions.merge())
        }
        batch.commit().await()

        for (mesa in mesasNoSincro) {
            appDao.marcarMesaComoSincronizada(mesa.id)
        }
    }

    private suspend fun bajarMesasDeFirebase() {
        val snapshot = firestore.collection("mesas").get().await()
        val mesasNube = snapshot.documents.mapNotNull { doc ->
            doc.toObject(MesaFirestore::class.java)?.toDomain()?.toEntity(sincronizado = true)
        }
        if (mesasNube.isNotEmpty()) appDao.inicializarMesas(mesasNube)
    }

    private suspend fun bajarCategoriasDeFirebase() {
        val snapshot = firestore.collection("categorias").get().await()
        val categorias = snapshot.documents.mapNotNull { doc ->
            doc.toObject(CategoriaFirestore::class.java)?.toDomain()?.toEntity()
        }
        if (categorias.isNotEmpty()) appDao.insertarCategorias(categorias)
    }

    private suspend fun bajarProductosDeFirebase() {
        val snapshot = firestore.collection("productos").get().await()
        val productos = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProductoFirestore::class.java)?.toDomain()?.toEntity(sincronizado = true)
        }
        if (productos.isNotEmpty()) appDao.insertarProductos(productos)
    }

    private suspend fun bajarInsumosDeFirebase() {
        try {
            val snapshot = firestore.collection("insumos").get().await()
            val insumos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(InsumoFirestore::class.java)?.toDomain()?.toEntity(sincronizado = true)
            }
            if (insumos.isNotEmpty()) appDao.insertarInsumos(insumos)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando insumos: ${e.message}")
        }
    }

    private suspend fun bajarRelacionesInsumosDeFirebase() {
        try {
            val snapshot = firestore.collection("producto_insumos").get().await()
            val relaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProductoInsumoFirestore::class.java)?.toDomain()?.toEntity()
            }
            if (relaciones.isNotEmpty()) appDao.insertarProductoInsumos(relaciones)
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando relaciones insumos: ${e.message}")
        }
    }

    private suspend fun bajarPedidosActivosDeFirebase() {
        try {
            val snapshot = firestore.collection("pedidos")
                .whereIn("estado", listOf("PENDIENTE", "PREPARADO"))
                .get().await()

            for (doc in snapshot.documents) {
                val pedidoId = doc.id
                val local = appDao.obtenerPedidoPorId(pedidoId)

                if (local == null || local.sincronizado) {
                    val pedido = doc.toObject(PedidoFirestore::class.java)?.toDomain() ?: continue
                    
                    // Asegurar que el nombre del usuario esté presente si falta
                    var nombreUsuario = pedido.usuarioNombre
                    if (nombreUsuario.isNullOrBlank() && pedido.usuarioId != null) {
                        nombreUsuario = obtenerNombreUsuarioSync(pedido.usuarioId)
                    }

                    val pedidoFinal = pedido.copy(usuarioNombre = nombreUsuario)
                    appDao.insertarPedido(pedidoFinal.toEntity(sincronizado = true))
                    
                    // Insertar detalles
                    val detallesEntities = pedidoFinal.detalles.map { 
                        it.toEntity(pedidoId)
                    }
                    appDao.eliminarDetallesPorPedido(pedidoId)
                    appDao.insertarDetallesPedido(detallesEntities)
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando pedidos: ${e.message}")
        }
    }

    private suspend fun obtenerNombreUsuarioSync(uid: String): String {
        return try {
            val userLocal = appDao.obtenerUsuarioPorId(uid)
            if (userLocal != null) {
                "${userLocal.nombres} ${userLocal.apellidos}".trim()
            } else {
                val userDoc = firestore.collection("usuarios").document(uid).get().await()
                val user = userDoc.toObject(UsuarioFirestore::class.java)?.toDomain()
                if (user != null) {
                    "${user.nombres} ${user.apellidos}".trim().ifEmpty { "Trabajador" }
                } else {
                    "Trabajador"
                }
            }
        } catch (e: Exception) { "Trabajador" }
    }

    private suspend fun bajarCajaAbiertaDeFirebase() {
        try {
            val snapshot = firestore.collection("cierres_caja")
                .whereEqualTo("estado", "ABIERTA")
                .limit(1)
                .get().await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                val caja = doc.toObject(CajaSesionFirestore::class.java)?.toDomain()
                if (caja != null) {
                    appDao.limpiarSesionesLocales()
                    appDao.abrirCajaLocal(caja.toEntity(sincronizado = true))
                }
            } else {
                appDao.limpiarSesionesLocales()
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando caja: ${e.message}")
        }
    }

    suspend fun bajarHistorialRango(usuarioId: String, inicio: Long, fin: Long) {
        try {
            val startTimestamp = Timestamp(Date(inicio))
            val endTimestamp = Timestamp(Date(fin))

            val snapshot = firestore.collection("pedidos")
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get().await()

            for (doc in snapshot.documents) {
                val pedidoId = doc.id
                val pedido = doc.toObject(PedidoFirestore::class.java)?.toDomain() ?: continue

                // Asegurar que el nombre del usuario esté presente if missing
                var nombreUsuario = pedido.usuarioNombre
                if (nombreUsuario.isNullOrBlank() && pedido.usuarioId != null) {
                    nombreUsuario = obtenerNombreUsuarioSync(pedido.usuarioId)
                }

                val pedidoFinal = pedido.copy(usuarioNombre = nombreUsuario)
                appDao.insertarPedido(pedidoFinal.toEntity(sincronizado = true))

                val detallesEntities = pedidoFinal.detalles.map { it.toEntity(pedidoId) }
                appDao.eliminarDetallesPorPedido(pedidoId)
                appDao.insertarDetallesPedido(detallesEntities)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando historial: ${e.message}")
        }
    }

    private suspend fun subirPedidosPendientes() {
        val pedidosPendientes = appDao.obtenerPedidosPendientesDeSincronizar()
        if (pedidosPendientes.isEmpty()) return

        for (pedido in pedidosPendientes) {
            val pedidoRef = firestore.collection("pedidos").document(pedido.pedidoId)
            val detallesEntities = appDao.obtenerDetallesPorPedido(pedido.pedidoId)
            val detallesArray = detallesEntities.map { det ->
                hashMapOf(
                    "productoId" to det.productoId,
                    "nombreProducto" to det.nombreProducto,
                    "cantidad" to det.cantidad,
                    "precioUnitario" to det.precioUnitario
                )
            }

            val datosPedido = hashMapOf(
                "pedidoId" to pedido.pedidoId,
                "numeroPedido" to pedido.numeroPedido,
                "fecha" to if (pedido.fecha != null) Timestamp(Date(pedido.fecha)) else Timestamp.now(),
                "estado" to pedido.estado?.name,
                "tipoPedido" to pedido.tipoPedido.name,
                "mesaId" to pedido.mesaId,
                "metodoPago" to pedido.metodoPago?.name,
                "nombreCliente" to pedido.nombreCliente,
                "total" to pedido.total,
                "usuarioId" to pedido.usuarioId,
                "usuarioNombre" to pedido.usuarioNombre,
                "notas" to pedido.notas,
                "cajaId" to pedido.cajaId,
                "detalles" to detallesArray,
                "sincronizado" to true
            )

            try {
                pedidoRef.set(datosPedido).await()
                appDao.marcarPedidoComoSincronizado(pedido.pedidoId)
            } catch (e: Exception) {
                Log.e("SyncManager", "Error subiendo pedido ${pedido.pedidoId}: ${e.message}")
            }
        }
    }
}
