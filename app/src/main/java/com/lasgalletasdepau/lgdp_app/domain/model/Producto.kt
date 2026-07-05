package com.lasgalletasdepau.lgdp_app.domain.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoriaId: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val controlaStock: Boolean = false,
    val estaDisponible: Boolean = true,
    val imagen: String = ""
)
