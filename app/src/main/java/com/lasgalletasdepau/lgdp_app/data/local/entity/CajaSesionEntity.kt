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
    val estado: String = "ABIERTA", // "ABIERTA" o "CERRADA"
    val sincronizado: Boolean = false
)