package com.lasgalletasdepau.lgdp_app.domain.model

data class Mesa(
    val id: Int,
    val numero: String,
    val estado: EstadoMesa,
    val clienteActivo: String? = null
)
