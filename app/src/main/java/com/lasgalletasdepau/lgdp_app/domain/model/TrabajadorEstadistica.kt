package com.lasgalletasdepau.lgdp_app.domain.model

data class TrabajadorEstadistica(
    val usuarioId: String,
    val nombre: String,
    val totalVendido: Double,
    val cantidadPedidos: Int,
    val porcentajeVentas: Float = 0f
)
