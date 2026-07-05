package com.lasgalletasdepau.lgdp_app.data.local

import androidx.room.TypeConverter
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido

class AppConverters {
    @TypeConverter
    fun fromEstadoMesa(value: EstadoMesa): String = value.name

    @TypeConverter
    fun toEstadoMesa(value: String): EstadoMesa = EstadoMesa.valueOf(value)

    @TypeConverter
    fun fromEstadoPedido(value: EstadoPedido?): String? = value?.name

    @TypeConverter
    fun toEstadoPedido(value: String?): EstadoPedido? = value?.let { EstadoPedido.valueOf(it) }

    @TypeConverter
    fun fromTipoPedido(value: TipoPedido): String = value.name

    @TypeConverter
    fun toTipoPedido(value: String): TipoPedido = TipoPedido.valueOf(value)

    @TypeConverter
    fun fromMetodoPago(value: MetodoPago?): String? = value?.name

    @TypeConverter
    fun toMetodoPago(value: String?): MetodoPago? = value?.let { 
        try { MetodoPago.valueOf(it) } catch(e: Exception) { null }
    }
    
    // Eliminado el converter de RolUsuario para manejarlo como String simple en la entidad
}