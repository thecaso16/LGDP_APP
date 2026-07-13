package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoEntity
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.firebase.Timestamp

class GestionCatalogoViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val appDao = AppDatabase.getDatabase(application).appDao()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _insumosDisponibles = MutableStateFlow<List<com.lasgalletasdepau.lgdp_app.domain.model.Insumo>>(emptyList())
    val insumosDisponibles: StateFlow<List<com.lasgalletasdepau.lgdp_app.domain.model.Insumo>> = _insumosDisponibles

    init {
        obtenerProductos()
        cargarInsumosParaVinculo()
    }

    private fun cargarInsumosParaVinculo() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("insumos").get().await()
                _insumosDisponibles.value = snapshot.documents.mapNotNull { it.toObject(com.lasgalletasdepau.lgdp_app.domain.model.Insumo::class.java)?.copy(id = it.id) }
            } catch (e: Exception) {}
        }
    }

    fun obtenerInsumosRelacionados(productoId: String, onResult: (List<com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoInsumoEntity>) -> Unit) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("producto_insumos")
                    .whereEqualTo("productoId", productoId)
                    .get().await()
                val lista = snapshot.documents.map { doc ->
                    com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoInsumoEntity(
                        productoId = doc.getString("productoId") ?: "",
                        insumoId = doc.getString("insumoId") ?: "",
                        cantidadRequerida = doc.getDouble("cantidadRequerida") ?: 0.0
                    )
                }
                onResult(lista)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun guardarVinculoInsumo(productoId: String, insumoId: String, cantidad: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val idDoc = "${productoId}_${insumoId}"
                val datos = hashMapOf(
                    "productoId" to productoId,
                    "insumoId" to insumoId,
                    "cantidadRequerida" to cantidad
                )
                firestore.collection("producto_insumos").document(idDoc).set(datos).await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarVinculoInsumo(productoId: String, insumoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val idDoc = "${productoId}_${insumoId}"
                firestore.collection("producto_insumos").document(idDoc).delete().await()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun obtenerProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Solo traemos los que NO están eliminados (activo == true)
                val snapshot = firestore.collection("productos")
                    .whereEqualTo("activo", true)
                    .get().await()
                
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                }
                _productos.value = lista
                
                // Sincronizar con SQLite local
                lista.forEach { p ->
                    appDao.insertarProductos(listOf(ProductoEntity(
                        productoId = p.id,
                        nombre = p.nombre,
                        descripcion = p.descripcion,
                        imagen = p.imagen,
                        precio = p.precio,
                        stock = p.stock,
                        controlaStock = p.controlaStock,
                        categoriaId = p.categoriaId,
                        estaDisponible = p.estaDisponible,
                        activo = p.activo,
                        recomendado = p.recomendado,
                        sincronizado = true,
                        ultimaActualizacion = p.ultimaActualizacion?.toDate()?.time ?: System.currentTimeMillis(),
                        operacionPendiente = null
                    )))
                }
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarProducto(producto: Producto, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
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
                obtenerProductos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarProductoLogico(productoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // En lugar de borrar, marcamos activo = false
                firestore.collection("productos").document(productoId)
                    .update(
                        "activo", false,
                        "ultimaActualizacion", Timestamp.now()
                    ).await()
                obtenerProductos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
