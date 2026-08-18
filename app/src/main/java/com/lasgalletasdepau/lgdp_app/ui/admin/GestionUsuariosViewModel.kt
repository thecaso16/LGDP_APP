package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GestionUsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(appDao, context = application)

    val usuarios: StateFlow<List<Usuario>> = usuarioRepository.obtenerUsuarios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun crearNuevoUsuarioConAuth(
        usuario: Usuario, 
        contrasena: String, 
        roles: List<String>, 
        onResult: (Boolean) -> Unit
    ) {
        if (usuario.email.isEmpty() || contrasena.length < 6) {
            _error.value = "El correo es obligatorio y la contraseña debe tener al menos 6 caracteres."
            onResult(false)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val rolString = roles.joinToString(",")
            val result = usuarioRepository.crearUsuarioAdmin(usuario.copy(rol = rolString), contrasena)
            result.fold(
                onSuccess = { onResult(true) },
                onFailure = { e -> 
                    _error.value = "Error al crear usuario: ${e.localizedMessage}"
                    onResult(false)
                }
            )
            _isLoading.value = false
        }
    }

    fun actualizarUsuarioFirestore(usuario: Usuario, roles: List<String>, onResult: (Boolean) -> Unit) {
        val rolString = roles.joinToString(",")
        viewModelScope.launch {
            _isLoading.value = true
            val result = usuarioRepository.actualizarUsuarioAdmin(usuario.copy(rol = rolString))
            result.fold(
                onSuccess = { onResult(true) },
                onFailure = { e -> 
                    _error.value = "Error al actualizar: ${e.localizedMessage}"
                    onResult(false)
                }
            )
            _isLoading.value = false
        }
    }

    fun eliminarUsuario(usuarioId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = usuarioRepository.eliminarUsuarioAdmin(usuarioId)
            onResult(result.isSuccess)
        }
    }
    
    fun clearError() { _error.value = null }
}
