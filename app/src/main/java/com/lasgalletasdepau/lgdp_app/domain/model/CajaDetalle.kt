package com.lasgalletasdepau.lgdp_app.domain.model

data class CajaDetalle(
    val cajaId: String,
    val fechaCierre: Long,
    val egresos: Double,
    val ingresosEfectivo: Double,
    val ingresosIzipay: Double,
    val ingresosBilleteraDigital: Double,
    val totalVentas: Double,
    val esperadoFisico: Double,
    val montoFisicoReal: Double,
    val diferencia: Double,
    val justificacion: String?
)
