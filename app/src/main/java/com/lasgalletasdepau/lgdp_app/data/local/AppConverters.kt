package com.lasgalletasdepau.lgdp_app.data.local

import androidx.room.TypeConverter
import com.lasgalletasdepau.lgdp_app.domain.model.*

class AppConverters {

    // --- ESTADO MESA ---
    @TypeConverter
    fun fromEstadoMesa(estado: EstadoMesa?): String? = estado?.name

    @TypeConverter
    fun toEstadoMesa(valor: String?): EstadoMesa? = valor?.let { enumValueOf<EstadoMesa>(it) }

    // --- ESTADO PEDIDO ---
    @TypeConverter
    fun fromEstadoPedido(estado: EstadoPedido?): String? = estado?.name

    @TypeConverter
    fun toEstadoPedido(valor: String?): EstadoPedido? = valor?.let { enumValueOf<EstadoPedido>(it) }

    // --- TIPO PEDIDO ---
    @TypeConverter
    fun fromTipoPedido(tipo: TipoPedido?): String? = tipo?.name

    @TypeConverter
    fun toTipoPedido(valor: String?): TipoPedido? = valor?.let { enumValueOf<TipoPedido>(it) }

    // --- MÉTODOPAGO ---
    @TypeConverter
    fun fromMetodoPago(metodo: MetodoPago?): String? = metodo?.name

    @TypeConverter
    fun toMetodoPago(valor: String?): MetodoPago? = valor?.let { enumValueOf<MetodoPago>(it) }

    // --- ROL USUARIO ---
    @TypeConverter
    fun fromRolUsuario(rol: RolUsuario?): String? = rol?.name

    @TypeConverter
    fun toRolUsuario(valor: String?): RolUsuario? = valor?.let { RolUsuario.fromString(it) }
}