package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val productoId: String,
    val nombre: String?,
    val descripcion: String?,
    val imagen: String?,
    val precio: Double,
    val stock: Int,
    val controlaStock: Boolean = false,
    val categoriaId: String?,
    val recomendado: Boolean,
    val estaDisponible: Boolean,
    val activo: Boolean,
    val sincronizado: Boolean,
    val ultimaActualizacion: Long?,
    val operacionPendiente: String?
)