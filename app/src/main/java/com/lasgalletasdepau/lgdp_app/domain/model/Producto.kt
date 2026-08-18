package com.lasgalletasdepau.lgdp_app.domain.model

data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String?,
    val categoriaId: String?,
    val precio: Double,
    val stock: Int,
    val controlaStock: Boolean,
    val estaDisponible: Boolean,
    val imagen: String?,
    val activo: Boolean,
    val recomendado: Boolean,
    val ultimaActualizacion: Long?
)
