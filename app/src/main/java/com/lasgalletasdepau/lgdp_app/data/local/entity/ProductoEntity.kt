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
    val categoriaId: String?,
    val recomendado: Boolean,
    val sincronizado: Boolean,
    val ultimaActualizacion: Long?, // Las fechas se guardan como Long (milisegundos) en SQLite
    val operacionPendiente: String?
)