package com.lasgalletasdepau.lgdp_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Usuario(
    val id: Int,
    val nombre: String,
    val celular: String,
    val rol: String, // "Administrador" o "Mozo"
    val estaActivo: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen() {
    // LISTA INICIAL DE PRUEBA
    var listaUsuarios by remember {
        mutableStateOf(
            listOf(
                Usuario(1, "Carlos Segura", "987654321", "Administrador", true),
                Usuario(2, "Ana Martínez", "912345678", "Mozo", true),
                Usuario(3, "Pedro Quispe", "955667788", "Mozo", false)
            )
        )
    }

    // EL ADMIN ACTUAL EN SESIÓN (Para evitar auto-eliminación)
    val idAdminActual = 1

    // ESTADOS PARA CREAR NUEVO
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoCelular by remember { mutableStateOf("") }
    var nuevoRol by remember { mutableStateOf("Mozo") }
    var nuevaPassword by remember { mutableStateOf("") }

    // ESTADOS PARA EDITAR / GESTIONAR EXISTENTE
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editCelular by remember { mutableStateOf("") }
    var editRol by remember { mutableStateOf("Mozo") }
    var editActivo by remember { mutableStateOf(true) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Personal 👥", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFormularioCrear = !mostrarFormularioCrear },
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White
            ) {
                Icon(if (mostrarFormularioCrear) Icons.Default.Close else Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // SECCIÓN: REGISTRAR NUEVO TRABAJADOR
                item {
                    AnimatedVisibility(visible = mostrarFormularioCrear) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Registrar Nuevo Trabajador", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D), fontSize = 16.sp)

                                OutlinedTextField(
                                    value = nuevoNombre,
                                    onValueChange = { nuevoNombre = it },
                                    label = { Text("Nombre Completo") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                // CAMPO CELULAR CON RESTRICCIÓN NUMÉRICA (MÁXIMO 9 DÍGITOS)
                                OutlinedTextField(
                                    value = nuevoCelular,
                                    onValueChange = { input ->
                                        // Solo permite dígitos y un máximo de 9 caracteres
                                        val digitsOnly = input.filter { it.isDigit() }
                                        if (digitsOnly.length <= 9) {
                                            nuevoCelular = digitsOnly
                                        }
                                    },
                                    label = { Text("Celular (9 dígitos)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = nuevaPassword,
                                    onValueChange = { nuevaPassword = it },
                                    label = { Text("Contraseña de Acceso") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Rol:", fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = nuevoRol == "Mozo", onClick = { nuevoRol = "Mozo" })
                                        Text("Mozo")
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = nuevoRol == "Administrador", onClick = { nuevoRol = "Administrador" })
                                        Text("Admin")
                                    }
                                }

                                Button(
                                    onClick = {
                                        // Validación extra: el celular debe tener exactamente 9 dígitos para guardarse
                                        if (nuevoNombre.isNotBlank() && nuevoCelular.length == 9) {
                                            val nuevoId = (listaUsuarios.maxOfOrNull { it.id } ?: 0) + 1
                                            listaUsuarios = listaUsuarios + Usuario(
                                                id = nuevoId,
                                                nombre = nuevoNombre,
                                                celular = nuevoCelular,
                                                rol = nuevoRol,
                                                estaActivo = true
                                            )
                                            nuevoNombre = ""
                                            nuevoCelular = ""
                                            nuevaPassword = ""
                                            mostrarFormularioCrear = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = nuevoNombre.isNotBlank() && nuevoCelular.length == 9 // Se deshabilita si no cumple
                                ) {
                                    Text("Guardar y Activar", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // LISTA DE TRABAJADORES
                item {
                    Text("Lista de Trabajadores (Toca para editar)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                items(listaUsuarios) { usuario ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                usuarioSeleccionado = usuario
                                editNombre = usuario.nombre
                                editCelular = usuario.celular
                                editRol = usuario.rol
                                editActivo = usuario.estaActivo
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFF1E233D).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50.dp),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E233D), modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E233D))
                                    Text(text = "📱 ${usuario.celular}", fontSize = 13.sp, color = Color.Gray)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = if (usuario.rol == "Administrador") Color(0xFF1E233D) else Color(0xFF10B981),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = usuario.rol,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = if (usuario.estaActivo) "🟢 Activo" else "🔴 Inactivo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (usuario.estaActivo) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE EDICIÓN Y GESTIÓN DE TRABAJADOR ---
    if (usuarioSeleccionado != null) {
        val esElMismoAdmin = usuarioSeleccionado?.id == idAdminActual

        AlertDialog(
            onDismissRequest = { usuarioSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Editar Trabajador", fontWeight = FontWeight.Black, color = Color(0xFF1E233D), fontSize = 18.sp)
                    if (!esElMismoAdmin) {
                        IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFB91C1C))
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // CAMPO CELULAR CON RESTRICCIÓN NUMÉRICA EN LA EDICIÓN
                    OutlinedTextField(
                        value = editCelular,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 9) {
                                editCelular = digitsOnly
                            }
                        },
                        label = { Text("Celular (9 dígitos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (!esElMismoAdmin) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rol:", fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = editRol == "Mozo", onClick = { editRol = "Mozo" })
                                Text("Mozo")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = editRol == "Administrador", onClick = { editRol = "Administrador" })
                                Text("Admin")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estado de Cuenta:", fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(if (editActivo) "Activo" else "Inactivo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = editActivo,
                                    onCheckedChange = { editActivo = it },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981))
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "ℹ️ Al ser tu cuenta de administrador actual, no puedes cambiar tu rol ni desactivar tu estado.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNombre.isNotBlank() && editCelular.length == 9) {
                            listaUsuarios = listaUsuarios.map {
                                if (it.id == usuarioSeleccionado?.id) {
                                    it.copy(nombre = editNombre, celular = editCelular, rol = editRol, estaActivo = editActivo)
                                } else it
                            }
                            usuarioSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = editNombre.isNotBlank() && editCelular.length == 9 // Validación para guardar la edición
                ) {
                    Text("Guardar Cambios")
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioSeleccionado = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // --- DIÁLOGO DE SEGURIDAD: CONFIRMAR ELIMINACIÓN ---
    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("¿Eliminar Trabajador?", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = { Text("Esta acción quitará a ${usuarioSeleccionado?.nombre} de los registros de personal de forma definitiva.") },
            confirmButton = {
                Button(
                    onClick = {
                        listaUsuarios = listaUsuarios.filterNot { it.id == usuarioSeleccionado?.id }
                        mostrarConfirmarEliminar = false
                        usuarioSeleccionado = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Eliminar de Verdad", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GestionUsuariosScreenPreview() {
    GestionUsuariosScreen()
}