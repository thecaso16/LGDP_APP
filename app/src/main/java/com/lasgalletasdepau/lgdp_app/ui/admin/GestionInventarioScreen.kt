package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.Insumo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInventarioScreen(
    viewModel: GestionInventarioViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val insumos by viewModel.insumos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarFormularioCrear by remember { mutableStateOf(false) }

    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaCant by remember { mutableStateOf("") }
    var nuevaMin by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Kg") }

    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var editCant by remember { mutableStateOf("") }
    var editMin by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                if (isLoading && insumos.isEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
                }
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Inventario de Insumos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Controle las existencias de materia prima:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar insumo...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                AnimatedVisibility(visible = mostrarFormularioCrear) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Nuevo Insumo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre del insumo") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(value = nuevaCant, onValueChange = { nuevaCant = it }, label = { Text("Stock actual") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp))
                                OutlinedTextField(value = nuevaMin, onValueChange = { nuevaMin = it }, label = { Text("Stock mínimo") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp))
                            }
                                                        Text("Unidad de medida:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 4.dp)) {
                                    items(listOf("Kg", "Litros", "Unidades")) { u ->
                                        FilterChip(
                                            selected = unidad == u, 
                                            onClick = { unidad = u }, 
                                            label = { Text(u) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                            Button(
                                onClick = {
                                    val c = nuevaCant.toDoubleOrNull() ?: 0.0
                                    val m = nuevaMin.toDoubleOrNull() ?: 0.0
                                    viewModel.guardarInsumo(Insumo(nombre = nuevoNombre, cantidadActual = c, cantidadMinima = m, unidadMedida = unidad)) {
                                        if (it) { nuevoNombre = ""; nuevaCant = ""; nuevaMin = ""; mostrarFormularioCrear = false }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Guardar Insumo") }
                        }
                    }
                }
            }

            val filtrados = insumos.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
            items(filtrados) { insumo ->
                val colorEstado = when {
                    insumo.cantidadActual <= 0 -> Color.Red
                    insumo.cantidadActual <= insumo.cantidadMinima -> Color(0xFFD97706)
                    else -> MaterialTheme.colorScheme.secondary
                }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable {
                        insumoSeleccionado = insumo
                        editCant = insumo.cantidadActual.toString()
                        editMin = insumo.cantidadMinima.toString()
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(colorEstado, CircleShape))
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(insumo.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Mínimo: ${insumo.cantidadMinima} ${insumo.unidadMedida}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${insumo.cantidadActual} ${insumo.unidadMedida}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = colorEstado)
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
    }

    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Gestionar Insumo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(insumoSeleccionado?.nombre ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(value = editCant, onValueChange = { editCant = it }, label = { Text("Cantidad actual") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editMin, onValueChange = { editMin = it }, label = { Text("Stock mínimo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = editCant.toDoubleOrNull() ?: 0.0
                        val m = editMin.toDoubleOrNull() ?: 0.0
                        viewModel.guardarInsumo(insumoSeleccionado!!.copy(cantidadActual = c, cantidadMinima = m)) { if (it) insumoSeleccionado = null }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Guardar Cambios") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.eliminarInsumo(insumoSeleccionado!!.id) { if (it) insumoSeleccionado = null } }) { Text("Eliminar", color = Color.Red) }
            }
        )
    }
}
