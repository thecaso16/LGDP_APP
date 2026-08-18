package com.lasgalletasdepau.lgdp_app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Representación de los estados por los que pasa la pantalla
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val rol: String) : LoginState()
    data class Error(val mensaje: String) : LoginState()
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val mensaje: String) : ResetPasswordState()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(appDao, context = application)

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState

    init {
        // Verificar si ya hay una sesión activa en Firebase y en Room
        verificarSesionExistente()
    }

    private fun verificarSesionExistente() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Verificamos si el usuario en Room coincide con el de Firebase
                val localUser = usuarioRepository.obtenerUsuarioLogueadoSync()
                if (localUser != null && localUser.id == currentUser.uid) {
                    _loginState.value = LoginState.Success(localUser.rol)
                } else {
                    // Si no está en Room pero sí en Firebase, lo recuperamos
                    val user = usuarioRepository.recuperarDatosUsuarioRemoto(currentUser.uid)
                    if (user != null) {
                        usuarioRepository.loginLocal(user)
                        _loginState.value = LoginState.Success(user.rol)
                    } else {
                        _loginState.value = LoginState.Idle
                    }
                }
            } else {
                _loginState.value = LoginState.Idle
            }
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _loginState.value = LoginState.Error("Por favor, completa todos los campos.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            android.util.Log.d("LoginVM", "Intentando iniciar sesión para: $correo")
            val result = usuarioRepository.loginRemoto(correo, contrasena)
            result.fold(
                onSuccess = { user ->
                    android.util.Log.d("LoginVM", "Login exitoso, Rol: ${user.rol}")
                    _loginState.value = LoginState.Success(user.rol)
                },
                onFailure = { e ->
                    android.util.Log.e("LoginVM", "Error en loginRemoto: ${e.message}")
                    val mensajeError = when {
                        e.message?.contains("password", ignoreCase = true) == true -> "Contraseña incorrecta."
                        e.message?.contains("user", ignoreCase = true) == true -> "Usuario no encontrado."
                        else -> "Error: ${e.localizedMessage ?: "Credenciales inválidas o error de conexión."}"
                    }
                    _loginState.value = LoginState.Error(mensajeError)
                }
            )
        }
    }

    fun enviarCorreoRecuperacion(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = ResetPasswordState.Error("Ingresa tu correo.")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            val result = usuarioRepository.enviarCorreoRecuperacion(email)
            result.fold(
                onSuccess = { _resetPasswordState.value = ResetPasswordState.Success },
                onFailure = { e -> _resetPasswordState.value = ResetPasswordState.Error(e.localizedMessage ?: "Error al enviar correo.") }
            )
        }
    }

    fun resetearEstadoPassword() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun cerrarSesion() {
        _loginState.value = LoginState.Idle
        viewModelScope.launch {
            usuarioRepository.cerrarSesion()
        }
    }

    fun resetearEstado() {
        _loginState.value = LoginState.Idle
    }
}