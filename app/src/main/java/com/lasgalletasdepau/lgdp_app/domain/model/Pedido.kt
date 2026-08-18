package com.lasgalletasdepau.lgdp_app.domain.model

data class Pedido(
    val pedidoId: String,
    val numeroPedido: Int,
    val fecha: Long?,
    val estado: EstadoPedido?,
    val tipoPedido: TipoPedido,
    val mesaId: Int?,
    val metodoPago: MetodoPago?,
    val nombreCliente: String?,
    val total: Double,
    val usuarioId: String?,
    val usuarioNombre: String?,
    val notas: String?,
    val cajaId: String?,
    val detalles: List<PedidoDetalle> = emptyList()
)

data class PedidoDetalle(
    val productoId: String?,
    val nombreProducto: String?,
    val cantidad: Int,
    val precioUnitario: Double,
    val comentario: String?
)
