package com.lasgalletasdepau.lgdp_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

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
                    // 2. Buscar el rol en Firestore dentro de la colección "usuarios"
                    val document = firestore.collection("usuarios").document(uid).get().await()

                    if (document.exists()) {
                        val rolObtenido = document.getString("rol") ?: "Trabajador"
                        _loginState.value = LoginState.Success(rolObtenido)
                    } else {
                        _loginState.value = LoginState.Error("El usuario no está registrado en la base de datos.")
                    }
                } else {
                    _loginState.value = LoginState.Error("No se pudo obtener el identificador del usuario.")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.localizedMessage ?: "Ocurrió un error inesperado.")
            }
        }
    }

    fun resetearEstado() {
        _loginState.value = LoginState.Idle
    }
}