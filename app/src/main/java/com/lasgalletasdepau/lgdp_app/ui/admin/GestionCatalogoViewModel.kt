package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GestionCatalogoViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        obtenerProductos()
    }

    fun obtenerProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("productos").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Producto::class.java)?.copy(id = doc.id)
                }
                _productos.value = lista
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarProducto(producto: Producto, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (producto.id.isEmpty()) {
                    firestore.collection("productos").add(producto).await()
                } else {
                    firestore.collection("productos").document(producto.id).set(producto).await()
                }
                obtenerProductos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarProducto(productoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("productos").document(productoId).delete().await()
                obtenerProductos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
