package com.lasgalletasdepau.lgdp_app.data.repository

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.mapper.toDomain
import com.lasgalletasdepau.lgdp_app.data.mapper.toEntity
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import com.lasgalletasdepau.lgdp_app.data.remote.UsuarioFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UsuarioRepositoryImpl(
    private val appDao: AppDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val context: Context? = null
) : UsuarioRepository {

    private fun mapDocumentToUsuario(doc: com.google.firebase.firestore.DocumentSnapshot): Usuario? {
        return try {
            // 1. Intentar con el DTO (lo más limpio)
            doc.toObject(UsuarioFirestore::class.java)?.toDomain()
        } catch (e: Exception) {
            android.util.Log.w("UsuarioRepo", "Error con toObject en ${doc.id}, usando fallback manual: ${e.message}")
            // 2. Fallback Manual si toObject falla
            try {
                val id = doc.id
                val nombres = doc.getString("nombres") ?: ""
                val apellidos = doc.getString("apellidos") ?: ""
                val dni = doc.getString("dni") ?: ""
                val email = doc.getString("email") ?: ""
                val rol = doc.getString("rol") ?: "Trabajador"
                val activo = doc.getBoolean("activo") ?: true

                // Manejo híbrido de fechas (Timestamp o Long)
                val creadoEn = try { doc.getTimestamp("creadoEn")?.toDate()?.time } catch (ex: Exception) { doc.getLong("creadoEn") }
                val ultimaModificacion = try { doc.getTimestamp("ultimaModificacion")?.toDate()?.time } catch (ex: Exception) { doc.getLong("ultimaModificacion") }

                Usuario(id, nombres, apellidos, dni, email, rol, activo, creadoEn, ultimaModificacion)
            } catch (ex2: Exception) {
                android.util.Log.e("UsuarioRepo", "Falla crítica al mapear usuario ${doc.id}: ${ex2.message}")
                null
            }
        }
    }

    override fun obtenerUsuarioLogueado(): Flow<Usuario?> {
        return appDao.obtenerUsuarioLogueado().map { it?.toDomain() }
    }

    override suspend fun obtenerUsuarioLogueadoSync(): Usuario? {
        return appDao.obtenerUsuarioLogueadoSync()?.toDomain()
    }

    override suspend fun loginLocal(usuario: Usuario) {
        appDao.suplantarUsuario(usuario.toEntity(esSesionActual = true))
    }

    override suspend fun cerrarSesion() {
        auth.signOut()
        appDao.cerrarSesionLocal()
    }

    override suspend fun loginRemoto(email: String, contrasena: String): Result<Usuario> {
        return try {
            android.util.Log.d("UsuarioRepo", "Iniciando login remoto para: $email")
            val authResult = auth.signInWithEmailAndPassword(email, contrasena).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("UID nulo"))
            
            android.util.Log.d("UsuarioRepo", "Auth exitoso, recuperando datos para UID: $uid")
            val user = recuperarDatosUsuarioRemoto(uid)
            if (user != null) {
                android.util.Log.d("UsuarioRepo", "Usuario recuperado: ${user.email}, Rol: ${user.rol}")
                loginLocal(user)
                Result.success(user)
            } else {
                android.util.Log.e("UsuarioRepo", "No se encontraron datos del documento para el UID: $uid")
                Result.failure(Exception("Usuario no encontrado en base de datos de Firestore"))
            }
        } catch (e: Exception) {
            android.util.Log.e("UsuarioRepo", "Error en loginRemoto: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun recuperarDatosUsuarioRemoto(uid: String): Usuario? {
        return try {
            val doc = firestore.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                mapDocumentToUsuario(doc)
            } else {
                android.util.Log.w("UsuarioRepo", "Documento de usuario no existe en Firestore para UID: $uid")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UsuarioRepo", "Error recuperando usuario remoto: ${e.message}")
            null
        }
    }

    override suspend fun enviarCorreoRecuperacion(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerNombreUsuario(usuarioId: String): String {
        if (usuarioId == "desconocido") return "Usuario no identificado"
        
        // 1. Intentar local
        val userLocal = appDao.obtenerUsuarioPorId(usuarioId)
        if (userLocal != null) {
            return "${userLocal.nombres} ${userLocal.apellidos}".trim()
        }

        // 2. Intentar Firestore
        return try {
            val userDoc = firestore.collection("usuarios").document(usuarioId).get().await()
            val user = userDoc.toObject(UsuarioFirestore::class.java)?.toDomain()
            if (user != null) {
                "${user.nombres} ${user.apellidos}".trim().ifEmpty { "ID: ${usuarioId.takeLast(6)}" }
            } else {
                "ID: ${usuarioId.takeLast(6)}"
            }
        } catch (e: Exception) {
            "ID: ${usuarioId.takeLast(6)}"
        }
    }

    override fun obtenerUsuarios(): Flow<List<Usuario>> {
        return flow {
            try {
                val snapshot = firestore.collection("usuarios").get().await()
                val lista = snapshot.documents.mapNotNull { doc ->
                    mapDocumentToUsuario(doc)
                }
                
                lista.forEach { u ->
                    val existente = appDao.obtenerUsuarioPorId(u.id)
                    appDao.insertarUsuario(u.toEntity(esSesionActual = existente?.esSesionActual ?: false))
                }
                emit(lista)
            } catch (e: Exception) {
                android.util.Log.e("UsuarioRepo", "Error obteniendo lista de usuarios: ${e.message}")
                emit(emptyList<Usuario>())
            }
        }
    }

    override suspend fun crearUsuarioAdmin(usuario: Usuario, contrasena: String): Result<Usuario> {
        if (context == null) return Result.failure(Exception("Contexto no disponible para creación de usuario"))
        
        return try {
            val secondaryApp = try {
                FirebaseApp.getInstance("Secondary")
            } catch (e: Exception) {
                val options = FirebaseApp.getInstance().options
                FirebaseApp.initializeApp(context, options, "Secondary")
            }

            val authSecondary = FirebaseAuth.getInstance(secondaryApp)
            val authResult = authSecondary.createUserWithEmailAndPassword(usuario.email, contrasena).await()
            val newUid = authResult.user?.uid ?: throw Exception("No se pudo obtener el UID")

            val ahora = System.currentTimeMillis()
            val usuarioFinal = usuario.copy(
                id = newUid, 
                activo = true,
                creadoEn = ahora,
                ultimaModificacion = ahora
            )

            firestore.collection("usuarios").document(newUid).set(usuarioFinal).await()
            appDao.insertarUsuario(usuarioFinal.toEntity())
            authSecondary.signOut()
            
            Result.success(usuarioFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarUsuarioAdmin(usuario: Usuario): Result<Unit> {
        return try {
            val ahora = System.currentTimeMillis()
            val usuarioFinal = usuario.copy(ultimaModificacion = ahora)
            firestore.collection("usuarios").document(usuarioFinal.id).set(usuarioFinal).await()
            val existente = appDao.obtenerUsuarioPorId(usuario.id)
            appDao.insertarUsuario(usuarioFinal.toEntity(esSesionActual = existente?.esSesionActual ?: false))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarUsuarioAdmin(usuarioId: String): Result<Unit> {
        return try {
            firestore.collection("usuarios").document(usuarioId).delete().await()
            // appDao no tiene eliminar usuario pero se puede agregar o simplemente ignorar local
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
