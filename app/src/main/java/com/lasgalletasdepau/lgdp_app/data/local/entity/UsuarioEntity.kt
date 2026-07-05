package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val nombres: String?,
    val apellidos: String?,
    val dni: String?,
    val rol: String?, // Soporta "Trabajador,Cajero" o roles individuales
    val activo: Boolean = true
)