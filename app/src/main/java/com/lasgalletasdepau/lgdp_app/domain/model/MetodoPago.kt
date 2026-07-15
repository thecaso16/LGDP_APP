package com.lasgalletasdepau.lgdp_app.domain.model


enum class MetodoPago(val valor: String) {
    EFECTIVO("Efectivo"),
    YAPE("Billetera Digital"),
    IZIPAY("Izipay")
}