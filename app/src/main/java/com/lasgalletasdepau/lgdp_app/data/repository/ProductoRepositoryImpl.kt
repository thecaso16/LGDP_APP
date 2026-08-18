package com.lasgalletasdepau.lgdp_app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.mapper.toDomain
import com.lasgalletasdepau.lgdp_app.data.mapper.toEntity
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ProductoRepositoryImpl(
    private val appDao: AppDao,
    private val syncManager: SyncManager,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductoRepository {

    override fun obtenerCategorias(): Flow<List<Categoria>> {
        return appDao.obtenerCategorias().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun agregarCategoria(nombre: String) {
        firestore.collection("categorias").add(mapOf("nombre" to nombre)).await()
    }

    override fun obtenerProductos(): Flow<List<Producto>> {
        return appDao.obtenerProductos().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun guardarProducto(producto: Producto) {
        val ahora = Timestamp.now()
        val map = hashMapOf(
            "nombre" to producto.nombre,
            "descripcion" to producto.descripcion,
            "precio" to producto.precio,
            "stock" to producto.stock,
            "controlaStock" to producto.controlaStock,
            "categoriaId" to producto.categoriaId,
            "estaDisponible" to producto.estaDisponible,
            "activo" to producto.activo,
            "recomendado" to producto.recomendado,
            "imagen" to (producto.imagen ?: ""),
            "ultimaActualizacion" to ahora
        )

        if (producto.id.isEmpty()) {
            firestore.collection("productos").add(map).await()
        } else {
            firestore.collection("productos").document(producto.id).set(map).await()
        }
        
        // Actualizar local
        appDao.insertarProductos(listOf(producto.toEntity(sincronizado = true)))
    }

    override suspend fun eliminarProductoLogico(productoId: String) {
        firestore.collection("productos").document(productoId)
            .update(
                "activo", false,
                "ultimaActualizacion", Timestamp.now()
            ).await()
        appDao.descontarStock(productoId, 0) // Solo para disparar actualización local si fuera necesario, o simplemente marcar inactivo en Room
        // Idealmente appDao debería tener un marcarInactivo
    }

    override fun obtenerInsumos(): Flow<List<Insumo>> {
        return appDao.obtenerInsumos().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun obtenerInsumosPorProducto(productoId: String): List<ProductoInsumo> {
        return appDao.obtenerInsumosPorProducto(productoId).map { it.toDomain() }
    }

    override suspend fun guardarVinculoInsumo(productoId: String, insumoId: String, cantidad: Double) {
        val idDoc = "${productoId}_${insumoId}"
        val datos = hashMapOf(
            "productoId" to productoId,
            "insumoId" to insumoId,
            "cantidadRequerida" to cantidad
        )
        firestore.collection("producto_insumos").document(idDoc).set(datos).await()
    }

    override suspend fun eliminarVinculoInsumo(productoId: String, insumoId: String) {
        val idDoc = "${productoId}_${insumoId}"
        firestore.collection("producto_insumos").document(idDoc).delete().await()
    }

    override suspend fun guardarInsumo(insumo: Insumo) {
        if (insumo.id.isEmpty()) {
            val data = hashMapOf(
                "nombre" to insumo.nombre,
                "cantidadActual" to insumo.cantidadActual,
                "cantidadMinima" to insumo.cantidadMinima,
                "unidadMedida" to insumo.unidadMedida,
                "categoria" to insumo.categoria
            )
            firestore.collection("insumos").add(data).await()
        } else {
            firestore.collection("insumos").document(insumo.id).set(insumo).await()
        }
    }

    override suspend fun eliminarInsumo(insumoId: String) {
        firestore.collection("insumos").document(insumoId).delete().await()
    }

    override suspend fun sincronizarCatalogo() {
        syncManager.sincronizarTodo()
    }
}
