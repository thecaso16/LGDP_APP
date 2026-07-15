package com.lasgalletasdepau.lgdp_app.domain.model


enum class MetodoPago(val valor: String) {
    EFECTIVO("Efectivo"),
    BILLETERA_DIGITAL("Billetera Digital"),
    IZIPAY("Izipay");

    companion object {
        fun fromString(nombre: String?): MetodoPago? {
            if (nombre == null) return null
            return try {
                valueOf(nombre)
            } catch (e: Exception) {
                // Compatibilidad con el nombre antiguo "YAPE"
                if (nombre == "YAPE") BILLETERA_DIGITAL else null
            }
        }
    }
}