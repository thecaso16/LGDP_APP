package com.lasgalletasdepau.lgdp_app.domain.model

data class CajaSesion(
    val cajaId: String,
    val usuarioCajeroId: String,
    val nombreCajero: String,
    val fechaApertura: Long,
    val montoApertura: Double,
    val estado: String // "ABIERTA", "CERRADA"
)
