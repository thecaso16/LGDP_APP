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

    // Función principal que llamaremos cuando vuelva el internet
    suspend fun sincronizarTodo() {
        try {
            Log.d("SyncManager", "Iniciando sincronización bidireccional...")
            // PRIMERO: Subir cambios locales (Pedidos, Stock y Mesas) para no perderlos al sobreescribir
            subirPedidosPendientes()
            subirActualizacionesDeStock()
            subirEstadoMesas()

            // SEGUNDO: Descargar estado actual de la nube
            bajarMesasDeFirebase()
            bajarCategoriasDeFirebase()
            bajarProductosDeFirebase()
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

    // --- 1. DESCARGAR DE LA NUBE A LOCAL (Download) ---
    private suspend fun bajarMesasDeFirebase() {
        Log.d("SyncManager", "Bajando mesas de Firebase...")
        // Lee la colección "mesas" de Firestore
        val snapshot = firestore.collection("mesas").get().await()
        Log.d("SyncManager", "Se encontraron ${snapshot.size()} mesas en la nube.")
        
        val mesasNube = snapshot.documents.mapNotNull { doc ->
            val id = doc.getLong("id")?.toInt() ?: return@mapNotNull null
            val numero = "Mesa ${id.toString().padStart(2, '0')}"
            val estadoString = doc.getString("estado") ?: "LIBRE"
            val cliente = doc.getString("clienteActivo")

            // Convertimos el String de Firebase a nuestro Enum
            val estadoEnum = try {
                EstadoMesa.valueOf(estadoString)
            } catch (e: Exception) {
                EstadoMesa.LIBRE
            }

            MesaEntity(
                id = id, 
                numero = numero, 
                estado = estadoEnum, 
                clienteActivo = cliente,
                sincronizado = true
            )
        }

        // Si encontró mesas en la nube, las guarda/actualiza en SQLite
        if (mesasNube.isNotEmpty()) {
            Log.d("SyncManager", "Actualizando ${mesasNube.size} mesas en Room local.")
            appDao.inicializarMesas(mesasNube)
        } else {
            Log.d("SyncManager", "No hay mesas en la nube para descargar.")
        }
    }

    private suspend fun bajarCategoriasDeFirebase() {
        val snapshot = firestore.collection("categorias").get().await()
        val categorias = snapshot.documents.map { doc ->
            CategoriaEntity(id = doc.id, nombre = doc.getString("nombre"))
        }
        if (categorias.isNotEmpty()) {
            appDao.insertarCategorias(categorias)
        }
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
        if (productos.isNotEmpty()) {
            appDao.insertarProductos(productos)
        }
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
                        sincronizado = true
                    )
                    appDao.insertarPedido(pedido)

                    // Bajar detalles del array "detalles" dentro del documento
                    val detallesNube = doc.get("detalles") as? List<Map<String, Any>>
                    if (detallesNube != null) {
                        val detallesEntities = detallesNube.map { map ->
                            PedidoDetalleEntity(
                                pedidoId = pedidoId,
                                productoId = map["productoId"] as? String,
                                nombreProducto = map["nombreProducto"] as? String,
                                cantidad = (map["cantidad"] as? Long)?.toInt() ?: 0,
                                precioUnitario = (map["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                                comentario = map["comentario"] as? String
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

    suspend fun bajarHistorialRango(usuarioId: String, inicio: Long, fin: Long) {
        try {
            Log.d("SyncManager", "Buscando historial en Firebase para $usuarioId...")
            val startTimestamp = Timestamp(Date(inicio))
            val endTimestamp = Timestamp(Date(fin))

            val snapshot = firestore.collection("pedidos")
                .whereEqualTo("usuarioId", usuarioId)
                .whereGreaterThanOrEqualTo("fecha", startTimestamp)
                .whereLessThanOrEqualTo("fecha", endTimestamp)
                .get().await()

            Log.d("SyncManager", "Se encontraron ${snapshot.size()} registros históricos.")

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

    // --- 2. SUBIR DE LOCAL A LA NUBE (Upload) ---
    private suspend fun subirPedidosPendientes() {
        val pedidosPendientes = appDao.obtenerPedidosPendientesDeSincronizar()
        if (pedidosPendientes.isEmpty()) return

        for (pedido in pedidosPendientes) {
            val pedidoRef = firestore.collection("pedidos").document(pedido.pedidoId)

            // Obtener los detalles de Room para subirlos como Array
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