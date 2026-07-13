package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GestionUsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val context = application.applicationContext

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
                
                // Sincronizar con Room localmente
                lista.forEach { u ->
                    appDao.insertarUsuario(UsuarioEntity(
                        uid = u.id,
                        email = u.email,
                        nombres = u.nombres,
                        apellidos = u.apellidos,
                        dni = u.dni,
                        rol = u.rol,
                        activo = u.activo,
                        creadoEn = u.creadoEn?.toDate()?.time,
                        ultimaModificacion = u.ultimaModificacion?.toDate()?.time
                    ))
                }
            } catch (e: Exception) {
                _error.value = "Error al obtener usuarios: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Crea un usuario en Firebase Auth y luego guarda sus datos en Firestore.
     * Usamos una instancia secundaria de Firebase para no cerrar la sesión del Administrador.
     */
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
            try {
                // 1. Inicializar instancia secundaria (si no existe)
                val secondaryApp = try {
                    FirebaseApp.getInstance("Secondary")
                } catch (e: Exception) {
                    val options = FirebaseApp.getInstance().options
                    FirebaseApp.initializeApp(context, options, "Secondary")
                }

                val authSecondary = FirebaseAuth.getInstance(secondaryApp)

                // 2. Crear usuario en Auth
                val authResult = authSecondary.createUserWithEmailAndPassword(usuario.email, contrasena).await()
                val newUid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID")

                // 3. Preparar datos para Firestore
                val rolString = roles.joinToString(",")
                val ahora = com.google.firebase.Timestamp.now()
                val usuarioFinal = usuario.copy(
                    id = newUid, 
                    rol = rolString, 
                    activo = true,
                    creadoEn = ahora,
                    ultimaModificacion = ahora
                )

                // 4. Guardar en Firestore (usando el UID de Auth como ID del documento)
                firestore.collection("usuarios").document(newUid).set(usuarioFinal).await()
                
                // 5. Guardar en Room
                actualizarLocal(usuarioFinal)

                // 6. Cerrar sesión de la cuenta creada en la instancia secundaria
                authSecondary.signOut()

                obtenerUsuarios()
                onResult(true)
            } catch (e: Exception) {
                Log.e("GestionUsuarios", "Error al crear usuario", e)
                _error.value = "Error al crear usuario: ${e.localizedMessage}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun actualizarUsuarioFirestore(usuario: Usuario, roles: List<String>, onResult: (Boolean) -> Unit) {
        val rolString = roles.joinToString(",")
        val ahora = com.google.firebase.Timestamp.now()

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val usuarioFinal = usuario.copy(
                    rol = rolString,
                    ultimaModificacion = ahora
                )
                firestore.collection("usuarios").document(usuarioFinal.id).set(usuarioFinal).await()
                actualizarLocal(usuarioFinal)
                obtenerUsuarios()
                onResult(true)
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.localizedMessage}"
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun actualizarLocal(u: Usuario) {
        appDao.insertarUsuario(UsuarioEntity(
            uid = u.id, email = u.email, nombres = u.nombres, 
            apellidos = u.apellidos, dni = u.dni, rol = u.rol, activo = u.activo,
            creadoEn = u.creadoEn?.toDate()?.time,
            ultimaModificacion = u.ultimaModificacion?.toDate()?.time
        ))
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
    
    fun clearError() { _error.value = null }
}
