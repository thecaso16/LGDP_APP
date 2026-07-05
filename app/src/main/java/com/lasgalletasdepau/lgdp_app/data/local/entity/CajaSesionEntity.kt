package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "caja_sesiones")
data class CajaSesionEntity(
    @PrimaryKey val cajaId: String,
    val usuarioCajeroId: String,
    val nombreCajero: String,
    val fechaApertura: Long,
    val montoApertura: Double,
    val fechaCierre: Long? = null,
    val egresos: Double = 0.0,
    val montoFisicoReal: Double = 0.0,
    val justificacion: String? = null,
    val estado: String = "ABIERTA", // "ABIERTA" o "CERRADA"
    val sincronizado: Boolean = false
)