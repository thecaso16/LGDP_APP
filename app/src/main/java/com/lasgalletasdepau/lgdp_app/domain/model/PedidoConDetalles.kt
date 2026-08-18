package com.lasgalletasdepau.lgdp_app.domain.model

data class PedidoConDetalles(
    val pedido: Pedido,
    val detalles: List<PedidoDetalle>
)
