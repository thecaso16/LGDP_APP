package com.lasgalletasdepau.lgdp_app.domain.model

enum class RolUsuario(val valor: String) {
    ADMINISTRADOR("Administrador"),
    CAJERO("Cajero"),
    TRABAJADOR("Trabajador");

    companion object {
        fun fromString(valor: String?): RolUsuario {
            return entries.find { it.valor.equals(valor, ignoreCase = true) || it.name.equals(valor, ignoreCase = true) }
                ?: TRABAJADOR
        }
        
        /**
         * Maneja una lista de roles si un usuario tiene múltiples asignaciones.
         * Ejemplo en Firebase: "Trabajador,Cajero"
         */
        fun fromStringList(valor: String?): List<RolUsuario> {
            if (valor.isNullOrBlank()) return listOf(TRABAJADOR)
            return valor.split(",").map { fromString(it.trim()) }.distinct()
        }
    }
}