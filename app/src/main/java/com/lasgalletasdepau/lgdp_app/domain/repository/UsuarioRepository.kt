package com.lasgalletasdepau.lgdp_app.domain.repository

import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface UsuarioRepository {
    fun obtenerUsuarioLogueado(): Flow<Usuario?>
    suspend fun obtenerUsuarioLogueadoSync(): Usuario?
    suspend fun loginLocal(usuario: Usuario)
    suspend fun cerrarSesion()
    suspend fun obtenerNombreUsuario(usuarioId: String): String
    suspend fun loginRemoto(email: String, contrasena: String): Result<Usuario>
    suspend fun recuperarDatosUsuarioRemoto(uid: String): Usuario?
    suspend fun enviarCorreoRecuperacion(email: String): Result<Unit>
    
    // Admin
    fun obtenerUsuarios(): Flow<List<Usuario>>
    suspend fun crearUsuarioAdmin(usuario: Usuario, contrasena: String): Result<Usuario>
    suspend fun actualizarUsuarioAdmin(usuario: Usuario): Result<Unit>
    suspend fun eliminarUsuarioAdmin(usuarioId: String): Result<Unit>
}
