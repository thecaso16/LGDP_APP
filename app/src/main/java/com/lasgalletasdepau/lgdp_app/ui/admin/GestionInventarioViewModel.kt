package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.domain.model.Insumo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GestionInventarioViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _insumos = MutableStateFlow<List<Insumo>>(emptyList())
    val insumos: StateFlow<List<Insumo>> = _insumos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        obtenerInsumos()
    }

    fun obtenerInsumos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("insumos").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Insumo::class.java)?.copy(id = doc.id)
                }
                _insumos.value = lista
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarInsumo(insumo: Insumo, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (insumo.id.isEmpty()) {
                    firestore.collection("insumos").add(insumo).await()
                } else {
                    firestore.collection("insumos").document(insumo.id).set(insumo).await()
                }
                obtenerInsumos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarInsumo(insumoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("insumos").document(insumoId).delete().await()
                obtenerInsumos()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
