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
         * Soporta separadores como coma (,) o barra diagonal (/).
         */
        fun fromStringList(valor: String?): List<RolUsuario> {
            if (valor.isNullOrBlank()) return listOf(TRABAJADOR)
            // Dividir por coma o por barra diagonal
            val items = valor.split(Regex("[,/]"))
            return items.map { fromString(it.trim()) }.distinct()
        }
    }
}