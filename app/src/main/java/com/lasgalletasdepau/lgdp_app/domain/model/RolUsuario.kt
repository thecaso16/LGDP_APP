package com.lasgalletasdepau.lgdp_app.domain.model

enum class RolUsuario(val valor: String) {
    ADMINISTRADOR("Administrador"),
    TRABAJADOR("Trabajador");

    companion object {
        fun fromString(valor: String?): RolUsuario {
            return values().find { it.valor.equals(valor, ignoreCase = true) || it.name.equals(valor, ignoreCase = true) }
                ?: TRABAJADOR
        }
    }
}