package com.lasgalletasdepau.lgdp_app.domain.model

enum class RolUsuario(val valor: String) {
    ADMINISTRADOR("Administrador"),
    TRABAJADOR("Trabajador")
}

enum class EstadoMesa(val valor: String) {
    LIBRE("Libre"),
    OCUPADA("Ocupada"),
    ATENDIDA("Atendida"),
    CUENTA_PENDIENTE("Cuenta Pendiente")
}

enum class EstadoPedido(val valor: String) {
    PENDIENTE("Pendiente"),
    PREPARADO("Preparado"),
    PAGADO("Pagado"),
    CANCELADO("Cancelado")
}

enum class TipoPedido(val valor: String) {
    EN_MESA("En Mesa"),
    PARA_LLEVAR("Para Llevar")
}

enum class MetodoPago(val valor: String) {
    EFECTIVO("Efectivo"),
    YAPE("Yape"),
    IZIPAY("Izipay")
}