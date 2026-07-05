package com.lasgalletasdepau.lgdp_app.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido
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

            bajarMesasDeFirebase()
            bajarCategoriasDeFirebase()
            bajarProductosDeFirebase()
            bajarCajaAbiertaDeFirebase()
            bajarPedidosActivosDeFirebase()
            Log.d("SyncManager", "Sincronización completada con éxito.")
        } catch (e: Exception) {
            Log.e("SyncManager", "Error en sincronización: ${e.message}")
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
            val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
            val numero = "Mesa ${id.toString().padStart(2, '0')}"
            val estadoString = doc.getString("estado") ?: "LIBRE"
            val cliente = doc.getString("clienteActivo")
            val estadoEnum = try { EstadoMesa.valueOf(estadoString) } catch (e: Exception) { EstadoMesa.LIBRE }

            MesaEntity(id = id, numero = numero, estado = estadoEnum, clienteActivo = cliente, sincronizado = true)
        }
        if (mesasNube.isNotEmpty()) appDao.inicializarMesas(mesasNube)
    }

    private suspend fun bajarCategoriasDeFirebase() {
        val snapshot = firestore.collection("categorias").get().await()
        val categorias = snapshot.documents.map { doc ->
            CategoriaEntity(id = doc.id, nombre = doc.getString("nombre"))
        }
        if (categorias.isNotEmpty()) appDao.insertarCategorias(categorias)
    }

    private suspend fun bajarProductosDeFirebase() {
        val snapshot = firestore.collection("productos").get().await()
        val productos = snapshot.documents.map { doc ->
            ProductoEntity(
                productoId = doc.id,
                nombre = doc.getString("nombre"),
                descripcion = doc.getString("descripcion"),
                imagen = doc.getString("imagen"),
                precio = doc.getDouble("precio") ?: 0.0,
                stock = doc.getLong("stock")?.toInt() ?: 0,
                categoriaId = doc.getString("categoriaId"),
                recomendado = doc.getBoolean("recomendado") ?: false,
                sincronizado = true,
                ultimaActualizacion = System.currentTimeMillis(),
                operacionPendiente = null
            )
        }
        if (productos.isNotEmpty()) appDao.insertarProductos(productos)
    }

    private suspend fun bajarPedidosActivosDeFirebase() {
        try {
            val snapshot = firestore.collection("pedidos")
                .whereIn("estado", listOf("PENDIENTE", "PREPARANDO", "LISTO"))
                .get().await()

            for (doc in snapshot.documents) {
                val pedidoId = doc.id
                val local = appDao.obtenerPedidoPorId(pedidoId)
                
                if (local == null || local.sincronizado) {
                    val firebaseTimestamp = doc.getTimestamp("fecha")
                    val firebaseFecha = firebaseTimestamp?.toDate()?.time ?: 0L
                    
                    val pedido = PedidoEntity(
                        pedidoId = pedidoId,
                        numeroPedido = doc.getLong("numeroPedido")?.toInt() ?: 0,
                        fecha = firebaseFecha,
                        estado = EstadoPedido.valueOf(doc.getString("estado") ?: "PENDIENTE"),
                        tipoPedido = TipoPedido.valueOf(doc.getString("tipoPedido") ?: "EN_MESA"),
                        mesaId = doc.getLong("mesaId")?.toInt(),
                        metodoPago = doc.getString("metodoPago")?.let { 
                            try { MetodoPago.valueOf(it) } catch(e: Exception) { null }
                        },
                        nombreCliente = doc.getString("nombreCliente"),
                        total = doc.getDouble("total") ?: 0.0,
                        usuarioId = doc.getString("usuarioId"),
                        notas = doc.getString("notas"),
                        cajaId = doc.getString("cajaId"),
                        sincronizado = true
                    )
                    appDao.insertarPedido(pedido)

                    val detallesNube = doc.get("detalles") as? List<Map<String, Any>>
                    if (detallesNube != null) {
                        val detallesEntities = detallesNube.map { map ->
                            PedidoDetalleEntity(
                                pedidoId = pedidoId,
                                productoId = map["productoId"] as? String,
                                nombreProducto = map["nombreProducto"] as? String,
                                cantidad = (map["cantidad"] as? Long)?.toInt() ?: 0,
                                precioUnitario = (map["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                                comentario = null
                            )
                        }
                        appDao.eliminarDetallesPorPedido(pedidoId)
                        appDao.insertarDetallesPedido(detallesEntities)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Error bajando pedidos: ${e.message}")
        }
    }

    private suspend fun bajarCajaAbiertaDeFirebase() {
        try {
            val snapshot = firestore.collection("cierres_caja")
                .whereEqualTo("estado", "ABIERTA")
                .limit(1)
                .get().await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                // CORRECCIÓN: Usar nombres de argumentos para evitar errores posicionales
                val sesion = CajaSesionEntity(
                    cajaId = doc.id,
                    usuarioCajeroId = doc.getString("usuarioCajeroId") ?: doc.getString("usuarioId") ?: "",
                    nombreCajero = doc.getString("usuarioCajeroNombre") ?: "Desconocido",
                    fechaApertura = doc.getTimestamp("fechaApertura")?.toDate()?.time ?: System.currentTimeMillis(),
                    montoApertura = doc.getDouble("montoApertura") ?: 0.0,
                    fechaCierre = null,
                    egresos = 0.0,
                    montoFisicoReal = 0.0,
                    justificacion = null,
                    estado = "ABIERTA",
                    sincronizado = true
                )
                appDao.limpiarSesionesLocales()
                appDao.abrirCajaLocal(sesion)
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
                val firebaseTimestamp = doc.getTimestamp("fecha")
                val firebaseFecha = firebaseTimestamp?.toDate()?.time ?: 0L

                val pedido = PedidoEntity(
                    pedidoId = pedidoId,
                    numeroPedido = doc.getLong("numeroPedido")?.toInt() ?: 0,
                    fecha = firebaseFecha,
                    estado = doc.getString("estado")?.let { try { EstadoPedido.valueOf(it) } catch(e: Exception) { null } },
                    tipoPedido = doc.getString("tipoPedido")?.let { try { TipoPedido.valueOf(it) } catch(e: Exception) { TipoPedido.PARA_LLEVAR } } ?: TipoPedido.PARA_LLEVAR,
                    mesaId = doc.getLong("mesaId")?.toInt(),
                    metodoPago = doc.getString("metodoPago")?.let { try { MetodoPago.valueOf(it) } catch(e: Exception) { null } },
                    nombreCliente = doc.getString("nombreCliente"),
                    total = doc.getDouble("total") ?: 0.0,
                    usuarioId = doc.getString("usuarioId"),
                    notas = doc.getString("notas"),
                    cajaId = doc.getString("cajaId"),
                    sincronizado = true
                )
                appDao.insertarPedido(pedido)

                val detallesNube = doc.get("detalles") as? List<Map<String, Any>>
                if (detallesNube != null) {
                    val detallesEntities = detallesNube.map { map ->
                        PedidoDetalleEntity(
                            pedidoId = pedidoId,
                            productoId = map["productoId"] as? String,
                            nombreProducto = map["nombreProducto"] as? String,
                            cantidad = (map["cantidad"] as? Long)?.toInt() ?: 0,
                            precioUnitario = (map["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                            comentario = null
                        )
                    }
                    appDao.eliminarDetallesPorPedido(pedidoId)
                    appDao.insertarDetallesPedido(detallesEntities)
                }
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