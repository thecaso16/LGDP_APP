package com.lasgalletasdepau.lgdp_app.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.CategoriaEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoEntity
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import kotlinx.coroutines.tasks.await

class SyncManager(
    private val appDao: AppDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Función principal que llamaremos cuando vuelva el internet
    suspend fun sincronizarTodo() {
        try {
            Log.d("SyncManager", "Iniciando sincronización bidireccional...")
            // PRIMERO: Subir cambios locales (Pedidos y Stock) para no perderlos al sobreescribir
            subirPedidosPendientes()
            subirActualizacionesDeStock()

            // SEGUNDO: Descargar estado actual de la nube
            bajarMesasDeFirebase()
            bajarCategoriasDeFirebase()
            bajarProductosDeFirebase()
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

    // --- 1. DESCARGAR DE LA NUBE A LOCAL (Download) ---
    private suspend fun bajarMesasDeFirebase() {
        // Lee la colección "mesas" de Firestore
        val snapshot = firestore.collection("mesas").get().await()
        
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

            MesaEntity(id = id, numero = numero, estado = estadoEnum, clienteActivo = cliente)
        }

        // Si encontró mesas en la nube, las guarda/actualiza en SQLite
        if (mesasNube.isNotEmpty()) {
            appDao.inicializarMesas(mesasNube)
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

    // --- 2. SUBIR DE LOCAL A LA NUBE (Upload) ---
// Dentro de SyncManager.kt
    private suspend fun subirPedidosPendientes() {
        val pedidosPendientes = appDao.obtenerPedidosPendientesDeSincronizar()
        if (pedidosPendientes.isEmpty()) return

        val batch = firestore.batch()

        for (pedido in pedidosPendientes) {
            val pedidoRef = firestore.collection("pedidos").document(pedido.pedidoId)

            // 1. Subir Cabecera
            val datosPedido = hashMapOf(
                "pedidoId" to pedido.pedidoId,
                "numeroPedido" to pedido.numeroPedido,
                "fecha" to pedido.fecha,
                "estado" to pedido.estado?.name,
                "tipoPedido" to pedido.tipoPedido.name,
                "mesaId" to pedido.mesaId,
                "metodoPago" to pedido.metodoPago?.name,
                "nombreCliente" to pedido.nombreCliente,
                "total" to pedido.total,
                "usuarioId" to pedido.usuarioId,
                "sincronizado" to true
            )
            batch.set(pedidoRef, datosPedido)

            // 2. Subir Detalles (Obtenemos los detalles de Room)
            val detalles = appDao.obtenerDetallesPorPedido(pedido.pedidoId)
            for (detalle in detalles) {
                val detalleRef = pedidoRef.collection("detalles").document(detalle.idLocal.toString())
                batch.set(detalleRef, detalle)
            }
        }

        batch.commit().await()

        // 3. Marcar como sincronizado
        for (pedido in pedidosPendientes) {
            appDao.marcarPedidoComoSincronizado(pedido.pedidoId)
        }
    }
}