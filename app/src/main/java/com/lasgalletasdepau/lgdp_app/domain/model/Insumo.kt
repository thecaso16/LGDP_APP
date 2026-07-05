package com.lasgalletasdepau.lgdp_app.domain.model

data class Insumo(
    val id: String = "",
    val nombre: String = "",
    val cantidadActual: Double = 0.0,
    val cantidadMinima: Double = 0.0,
    val unidadMedida: String = "Kg",
    val categoria: String = ""
)
