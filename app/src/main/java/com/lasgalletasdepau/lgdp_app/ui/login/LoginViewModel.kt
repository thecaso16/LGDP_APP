package com.lasgalletasdepau.lgdp_app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val appDao = AppDatabase.getDatabase(application).appDao()

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
                val localUser = appDao.obtenerUsuarioPorId(currentUser.uid)
                if (localUser != null) {
                    // Nos aseguramos de que no haya otros usuarios residuales
                    appDao.suplantarUsuario(localUser)
                    _loginState.value = LoginState.Success(localUser.rol ?: "Trabajador")
                } else {
                    // Si no está en Room pero sí en Firebase, lo recuperamos
                    recuperarDatosUsuario(currentUser.uid)
                }
            } else {
                _loginState.value = LoginState.Idle
            }
        }
    }

    private suspend fun recuperarDatosUsuario(uid: String) {
        try {
            val document = firestore.collection("usuarios").document(uid).get().await()
            if (document.exists()) {
                val rol = document.getString("rol") ?: "Trabajador"
                val nombres = document.getString("nombres")
                val apellidos = document.getString("apellidos")
                val email = document.getString("email")
                val dni = document.getString("dni")

                val newUser = UsuarioEntity(
                    uid = uid,
                    email = email,
                    nombres = nombres,
                    apellidos = apellidos,
                    dni = dni,
                    rol = rol,
                    activo = true
                )
                
                // Limpiar y guardar el nuevo usuario
                appDao.suplantarUsuario(newUser)
                _loginState.value = LoginState.Success(rol)
            }
        } catch (e: Exception) {
            _loginState.value = LoginState.Error("Error al recuperar sesión: ${e.message}")
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _loginState.value = LoginState.Error("Por favor, completa todos los campos.")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // 1. Intentar ingresar con Firebase Auth
                val authResult = auth.signInWithEmailAndPassword(correo, contrasena).await()
                val uid = authResult.user?.uid

                if (uid != null) {
                    // 2. Buscar el rol y datos en Firestore
                    val document = firestore.collection("usuarios").document(uid).get().await()

                    if (document.exists()) {
                        val rolObtenido = document.getString("rol") ?: "Trabajador"
                        
                        val userToSave = UsuarioEntity(
                            uid = uid,
                            email = correo,
                            nombres = document.getString("nombres"),
                            apellidos = document.getString("apellidos"),
                            dni = document.getString("dni"),
                            rol = rolObtenido,
                            activo = true
                        )

                        // 3. Persistencia Local en Room (Limpiando previos)
                        appDao.suplantarUsuario(userToSave)

                        _loginState.value = LoginState.Success(rolObtenido)
                    } else {
                        _loginState.value = LoginState.Error("El usuario no está registrado en la base de datos.")
                    }
                } else {
                    _loginState.value = LoginState.Error("No se pudo obtener el identificador del usuario.")
                }
            } catch (e: Exception) {
                val mensajeError = when {
                    e.message?.contains("password", ignoreCase = true) == true -> "Contraseña incorrecta."
                    e.message?.contains("user", ignoreCase = true) == true -> "Usuario no encontrado."
                    else -> "Credenciales inválidas o error de conexión."
                }
                _loginState.value = LoginState.Error(mensajeError)
            }
        }
    }

    fun enviarCorreoRecuperacion(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = ResetPasswordState.Error("Ingresa tu correo.")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _resetPasswordState.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.localizedMessage ?: "Error al enviar correo.")
            }
        }
    }

    fun resetearEstadoPassword() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    fun cerrarSesion() {
        _loginState.value = LoginState.Idle
        auth.signOut()
        viewModelScope.launch {
            appDao.cerrarSesionLocal()
        }
    }

    fun resetearEstado() {
        _loginState.value = LoginState.Idle
    }
}