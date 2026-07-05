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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import com.lasgalletasdepau.lgdp_app.ui.admin.GestionUsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(viewModel: GestionUsuariosViewModel = viewModel()) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ESTADOS PARA CREAR NUEVO
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombres by remember { mutableStateOf("") }
    var nuevoApellidos by remember { mutableStateOf("") }
    var nuevoDni by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }
    var nuevoRol by remember { mutableStateOf("Trabajador") }

    // ESTADOS PARA EDITAR
    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var editNombres by remember { mutableStateOf("") }
    var editApellidos by remember { mutableStateOf("") }
    var editDni by remember { mutableStateOf("") }
    var editRol by remember { mutableStateOf("") }
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            if (isLoading && usuarios.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    AnimatedVisibility(visible = mostrarFormularioCrear) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Registrar Nuevo Trabajador", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                                OutlinedTextField(value = nuevoNombres, onValueChange = { nuevoNombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nuevoApellidos, onValueChange = { nuevoApellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nuevoDni, onValueChange = { if (it.length <= 8) nuevoDni = it.filter { c -> c.isDigit() } }, label = { Text("DNI") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = nuevoEmail, onValueChange = { nuevoEmail = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())

                                Text("Rol:", fontWeight = FontWeight.Medium)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("Trabajador", "Cajero", "Administrador").forEach { rol ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = nuevoRol == rol, onClick = { nuevoRol = rol })
                                            Text(rol, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        val u = Usuario(nombres = nuevoNombres, apellidos = nuevoApellidos, dni = nuevoDni, email = nuevoEmail, rol = nuevoRol, activo = true)
                                        viewModel.guardarUsuario(u) { if (it) { nuevoNombres = ""; nuevoApellidos = ""; nuevoDni = ""; mostrarFormularioCrear = false } }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                                    enabled = nuevoNombres.isNotBlank() && nuevoDni.length == 8
                                ) { Text("Guardar en Nube ☁️") }
                            }
                        }
                    }
                }

                item { Text("Personal en Firebase", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }

                items(usuarios) { usuario ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            usuarioSeleccionado = usuario
                            editNombres = usuario.nombres
                            editApellidos = usuario.apellidos
                            editDni = usuario.dni
                            editRol = usuario.rol
                            editActivo = usuario.activo
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E233D))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${usuario.nombres} ${usuario.apellidos}", fontWeight = FontWeight.Bold)
                                Text("Rol: ${usuario.rol}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(if (usuario.activo) "🟢" else "🔴")
                        }
                    }
                }
            }
        }
    }

    if (usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { usuarioSeleccionado = null },
            title = { Text("Editar Trabajador") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNombres, onValueChange = { editNombres = it }, label = { Text("Nombres") })
                    OutlinedTextField(value = editApellidos, onValueChange = { editApellidos = it }, label = { Text("Apellidos") })
                    Text("Rol:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Trabajador", "Cajero", "Administrador").forEach { rol ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = editRol == rol, onClick = { editRol = rol })
                                Text(rol, fontSize = 10.sp)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activo")
                        Switch(checked = editActivo, onCheckedChange = { editActivo = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val original = usuarioSeleccionado!!
                    val editado = original.copy(nombres = editNombres, apellidos = editApellidos, rol = editRol, activo = editActivo)
                    viewModel.guardarUsuario(editado) { if (it) usuarioSeleccionado = null }
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = true }) { Text("Eliminar", color = Color.Red) }
            }
        )
    }

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("¿Eliminar?") },
            text = { Text("Esta acción quitará al usuario de la base de datos.") },
            confirmButton = {
                Button(onClick = {
                    usuarioSeleccionado?.id?.let { id ->
                        viewModel.eliminarUsuario(id) { if (it) { mostrarConfirmarEliminar = false; usuarioSeleccionado = null } }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar") }
            }
        )
    }
}
