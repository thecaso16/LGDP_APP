package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "caja_detalles",
    foreignKeys = [
        ForeignKey(
            entity = CajaSesionEntity::class,
            parentColumns = ["cajaId"],
            childColumns = ["cajaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CajaDetalleEntity(
    @PrimaryKey val cajaId: String,
    val fechaCierre: Long? = null,
    val egresos: Double = 0.0,
    val ingresosEfectivo: Double = 0.0,
    val ingresosIzipay: Double = 0.0,
    val ingresosYape: Double = 0.0,
    val totalVentas: Double = 0.0,
    val esperadoFisico: Double = 0.0,
    val montoFisicoReal: Double = 0.0,
    val diferencia: Double = 0.0,
    val justificacion: String? = null
)