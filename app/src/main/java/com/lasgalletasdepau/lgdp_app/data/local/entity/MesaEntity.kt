package com.lasgalletasdepau.lgdp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa

@Entity(tableName = "mesas")
data class MesaEntity(
    @PrimaryKey val id: Int,
    val numero: String,
    val estado: EstadoMesa
)