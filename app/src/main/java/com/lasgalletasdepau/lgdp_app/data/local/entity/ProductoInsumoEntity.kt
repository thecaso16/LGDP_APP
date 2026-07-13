package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "producto_insumos",
    primaryKeys = ["productoId", "insumoId"],
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["productoId"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InsumoEntity::class,
            parentColumns = ["id"],
            childColumns = ["insumoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("insumoId")]
)
data class ProductoInsumoEntity(
    val productoId: String,
    val insumoId: String,
    val cantidadRequerida: Double // Cuánto de este insumo se gasta por cada unidad de producto
)