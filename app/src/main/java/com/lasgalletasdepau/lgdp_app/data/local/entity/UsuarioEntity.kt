package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val uid: String,
    val email: String?,
    val nombres: String?,
    val apellidos: String?,
    val dni: String?,
    val rol: RolUsuario,
    val activo: Boolean
)