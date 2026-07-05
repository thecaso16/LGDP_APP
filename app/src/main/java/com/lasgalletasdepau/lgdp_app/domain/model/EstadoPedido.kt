package com.lasgalletasdepau.lgdp_app.domain.model

enum class EstadoPedido(val valor: String) {
    PENDIENTE("Pendiente"),
    PREPARADO("Preparado"),
    PAGADO("Pagado"),
    CANCELADO("Cancelado")
}