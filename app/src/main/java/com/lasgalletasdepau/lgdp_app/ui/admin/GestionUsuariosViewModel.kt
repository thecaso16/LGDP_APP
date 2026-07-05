package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GestionUsuariosViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        obtenerUsuarios()
    }

    fun obtenerUsuarios() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val snapshot = firestore.collection("usuarios").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Usuario::class.java)?.copy(id = doc.id)
                }
                _usuarios.value = lista
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun guardarUsuario(usuario: Usuario, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                if (usuario.id.isEmpty()) {
                    firestore.collection("usuarios").add(usuario).await()
                } else {
                    firestore.collection("usuarios").document(usuario.id).set(usuario).await()
                }
                obtenerUsuarios()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarUsuario(usuarioId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                firestore.collection("usuarios").document(usuarioId).delete().await()
                obtenerUsuarios()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
