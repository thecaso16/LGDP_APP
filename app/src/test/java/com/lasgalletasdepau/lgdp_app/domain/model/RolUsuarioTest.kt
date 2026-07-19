package com.lasgalletasdepau.lgdp_app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RolUsuarioTest {

    @Test
    fun `fromString should return correct role regardless of case`() {
        assertEquals(RolUsuario.ADMINISTRADOR, RolUsuario.fromString("administrador"))
        assertEquals(RolUsuario.ADMINISTRADOR, RolUsuario.fromString("ADMINISTRADOR"))
        assertEquals(RolUsuario.CAJERO, RolUsuario.fromString("Cajero"))
        assertEquals(RolUsuario.TRABAJADOR, RolUsuario.fromString("trabajador"))
    }

    @Test
    fun `fromString should return TRABAJADOR for unknown or null strings`() {
        assertEquals(RolUsuario.TRABAJADOR, RolUsuario.fromString(null))
        assertEquals(RolUsuario.TRABAJADOR, RolUsuario.fromString(""))
        assertEquals(RolUsuario.TRABAJADOR, RolUsuario.fromString("Unknown"))
    }

    @Test
    fun `fromStringList should handle multiple roles with comma`() {
        val input = "Administrador, Cajero"
        val expected = listOf(RolUsuario.ADMINISTRADOR, RolUsuario.CAJERO)
        assertEquals(expected, RolUsuario.fromStringList(input))
    }

    @Test
    fun `fromStringList should handle multiple roles with slash`() {
        val input = "Cajero/Trabajador"
        val expected = listOf(RolUsuario.CAJERO, RolUsuario.TRABAJADOR)
        assertEquals(expected, RolUsuario.fromStringList(input))
    }

    @Test
    fun `fromStringList should return TRABAJADOR if input is empty or null`() {
        assertEquals(listOf(RolUsuario.TRABAJADOR), RolUsuario.fromStringList(null))
        assertEquals(listOf(RolUsuario.TRABAJADOR), RolUsuario.fromStringList(""))
    }

    @Test
    fun `fromStringList should handle mixed separators and spaces`() {
        val input = "Administrador / Cajero , Trabajador"
        val expected = listOf(RolUsuario.ADMINISTRADOR, RolUsuario.CAJERO, RolUsuario.TRABAJADOR)
        assertEquals(expected, RolUsuario.fromStringList(input))
    }
}
