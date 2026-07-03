package com.lasgalletasdepau.lgdp_app.domain.model

data class Usuario(
    val id: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val email: String = "",
    val rol: String = "" // "Administrador" o "Trabajador"
)