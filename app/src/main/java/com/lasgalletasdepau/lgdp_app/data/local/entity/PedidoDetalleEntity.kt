package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pedido_detalles",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["pedidoId"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pedidoId")] // Agiliza las búsquedas
)
data class PedidoDetalleEntity(
    @PrimaryKey(autoGenerate = true) val idLocal: Int = 0, // ID interno para SQLite
    val pedidoId: String, // Relación con PedidoEntity
    val productoId: String?,
    val nombreProducto: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val comentario: String?
)