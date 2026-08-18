package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    viewModel: GestionUsuariosViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombres by remember { mutableStateOf("") }
    var nuevoApellidos by remember { mutableStateOf("") }
    var nuevoDni by remember { mutableStateOf("") }
    var nuevoEmail by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var rolesSeleccionados by remember { mutableStateOf(setOf("Trabajador")) }

    var usuarioSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var editNombres by remember { mutableStateOf("") }
    var editApellidos by remember { mutableStateOf("") }
    var editDni by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editActivo by remember { mutableStateOf(true) }
    var editRoles by remember { mutableStateOf(setOf<String>()) }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(), 
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 100.dp)
        ) {
            item {
                if (isLoading && usuarios.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), color = MaterialTheme.colorScheme.primary)
                }
                
                Column {
                    Text(
                        text = "Gestión de Personal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Administre los accesos y perfiles del equipo:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            item {
                AnimatedVisibility(visible = mostrarFormularioCrear) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Registrar Trabajador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = nuevoNombres, onValueChange = { nuevoNombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoApellidos, onValueChange = { nuevoApellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoDni, onValueChange = { if (it.length <= 8) nuevoDni = it.filter { c -> c.isDigit() } }, label = { Text("DNI") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoEmail, onValueChange = { nuevoEmail = it }, label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            
                            OutlinedTextField(
                                value = nuevaContrasena, 
                                onValueChange = { nuevaContrasena = it }, 
                                label = { Text("Contraseña (mín. 6 caract.)") }, 
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("Asignar roles:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("Trabajador", "Cajero", "Administrador").forEach { rol ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = rolesSeleccionados.contains(rol),
                                            onCheckedChange = { isChecked ->
                                                rolesSeleccionados = if (isChecked) rolesSeleccionados + rol else rolesSeleccionados - rol
                                            }
                                        )
                                        Text(rol, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val u = Usuario(
                                        id = "",
                                        nombres = nuevoNombres,
                                        apellidos = nuevoApellidos,
                                        dni = nuevoDni,
                                        email = nuevoEmail,
                                        rol = "",
                                        activo = true,
                                        creadoEn = null,
                                        ultimaModificacion = null
                                    )
                                    viewModel.crearNuevoUsuarioConAuth(u, nuevaContrasena, rolesSeleccionados.toList()) { if (it) { 
                                        nuevoNombres = ""; nuevoApellidos = ""; nuevoDni = ""; nuevoEmail = ""; nuevaContrasena = ""; mostrarFormularioCrear = false 
                                    } }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                enabled = nuevoNombres.isNotBlank() && nuevoEmail.contains("@") && nuevaContrasena.length >= 6 && rolesSeleccionados.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp)
                            ) { 
                                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                                else Text("Registrar Trabajador") 
                            }
                        }
                    }
                }
            }

            items(usuarios) { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        usuarioSeleccionado = usuario
                        editNombres = usuario.nombres
                        editApellidos = usuario.apellidos
                        editDni = usuario.dni
                        editEmail = usuario.email
                        editActivo = usuario.activo
                        editRoles = usuario.rol.split(Regex("[,/]")).filter { it.isNotBlank() }.toSet()
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${usuario.nombres} ${usuario.apellidos}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Roles: ${usuario.rol}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            color = if (usuario.activo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (usuario.activo) "Activo" else "Inactivo",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (usuario.activo) Color(0xFF2E7D32) else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarFormularioCrear = !mostrarFormularioCrear },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(if (mostrarFormularioCrear) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (usuarioSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { usuarioSeleccionado = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Editar Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DATOS PERSONALES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = editNombres, 
                            onValueChange = { editNombres = it }, 
                            label = { Text("Nombres") }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editApellidos, 
                            onValueChange = { editApellidos = it }, 
                            label = { Text("Apellidos") }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editDni, 
                            onValueChange = { if (it.length <= 8) editDni = it.filter { c -> c.isDigit() } }, 
                            label = { Text("DNI") }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = editEmail, 
                            onValueChange = { editEmail = it }, 
                            label = { Text("Correo electrónico") }, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ROLES ASIGNADOS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            listOf("Trabajador", "Cajero", "Administrador").forEach { rol ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            editRoles = if (editRoles.contains(rol)) editRoles - rol else editRoles + rol
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = editRoles.contains(rol), 
                                        onCheckedChange = { isChecked ->
                                            editRoles = if (isChecked) editRoles + rol else editRoles - rol
                                        }
                                    )
                                    Text(rol, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ESTADO DE ACCESO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            color = if (editActivo) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (editActivo) "Cuenta Activa" else "Cuenta Inactiva",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (editActivo) Color(0xFF2E7D32) else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                                Switch(
                                    checked = editActivo, 
                                    onCheckedChange = { editActivo = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF2E7D32)
                                    )
                                )
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = { mostrarConfirmarEliminar = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("ELIMINAR ESTE TRABAJADOR", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val editado = usuarioSeleccionado!!.copy(
                            nombres = editNombres, 
                            apellidos = editApellidos, 
                            dni = editDni,
                            email = editEmail,
                            activo = editActivo
                        )
                        viewModel.actualizarUsuarioFirestore(editado, editRoles.toList()) { if (it) usuarioSeleccionado = null }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("¿Eliminar trabajador?") },
            text = { Text("Esta acción borrará permanentemente al trabajador de la base de datos.") },
            confirmButton = {
                Button(onClick = {
                    usuarioSeleccionado?.id?.let { viewModel.eliminarUsuario(it) { if (it) { mostrarConfirmarEliminar = false; usuarioSeleccionado = null } } }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}
