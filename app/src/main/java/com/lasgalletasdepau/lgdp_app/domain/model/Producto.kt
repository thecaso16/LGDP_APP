package com.lasgalletasdepau.lgdp_app.domain.model

import com.google.firebase.Timestamp

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoriaId: String = "",
    val precio: Double = 0.0,
    val stock: Int = 0,
    val controlaStock: Boolean = false,
    val estaDisponible: Boolean = true,
    val imagen: String = "",
    val activo: Boolean = true,
    val recomendado: Boolean = false,
    val ultimaActualizacion: Timestamp? = null
)
