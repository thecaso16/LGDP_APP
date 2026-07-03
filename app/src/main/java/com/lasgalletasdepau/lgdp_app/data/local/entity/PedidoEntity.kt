package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val pedidoId: String,
    val numeroPedido: Int,
    val fecha: Long?,
    val estado: EstadoPedido?, // Enum
    val tipoPedido: TipoPedido, // Enum
    val mesaId: Int?,
    val metodoPago: MetodoPago?, // Enum
    val nombreCliente: String?,
    val total: Double,
    val usuarioId: String?,
    val sincronizado: Boolean
)