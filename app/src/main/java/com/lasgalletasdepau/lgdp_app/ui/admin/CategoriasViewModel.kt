package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Categoria(
    val id: String = "",
    val nombre: String = ""
)

class CategoriasViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias

    init {
        obtenerCategorias()
    }

    fun obtenerCategorias() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("categorias").get().await()
                val lista = snapshot.documents.map { doc ->
                    Categoria(id = doc.id, nombre = doc.getString("nombre") ?: "")
                }
                _categorias.value = lista
            } catch (e: Exception) {}
        }
    }

    fun agregarCategoria(nombre: String) {
        viewModelScope.launch {
            try {
                firestore.collection("categorias").add(mapOf("nombre" to nombre)).await()
                obtenerCategorias()
            } catch (e: Exception) {}
        }
    }
}
