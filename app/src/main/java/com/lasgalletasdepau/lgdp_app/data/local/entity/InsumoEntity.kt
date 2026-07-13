package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "insumos")
data class InsumoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val cantidadActual: Double,
    val cantidadMinima: Double,
    val unidadMedida: String,
    val categoria: String? = null,
    val sincronizado: Boolean = true
)