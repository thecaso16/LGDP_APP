package com.lasgalletasdepau.lgdp_app

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import com.lasgalletasdepau.lgdp_app.domain.model.Insumo
import com.lasgalletasdepau.lgdp_app.ui.admin.GestionInventarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInventarioScreen(viewModel: GestionInventarioViewModel = viewModel()) {
    val insumos by viewModel.insumos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarFormularioCrear by remember { mutableStateOf(false) }

    // ESTADOS CREAR
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaCant by remember { mutableStateOf("") }
    var nuevaMin by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Kg") }

    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var editCant by remember { mutableStateOf("") }
    var editMin by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Insumos 📦", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarFormularioCrear = !mostrarFormularioCrear }, containerColor = Color(0xFF1E233D), contentColor = Color.White) {
                Icon(if (mostrarFormularioCrear) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar insumo...") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            if (isLoading && insumos.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            AnimatedVisibility(visible = mostrarFormularioCrear) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nuevo Insumo en Firebase", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre Insumo") }, modifier = Modifier.fillMaxWidth())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = nuevaCant, onValueChange = { nuevaCant = it }, label = { Text("Stock Act") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                            OutlinedTextField(value = nuevaMin, onValueChange = { nuevaMin = it }, label = { Text("Mínimo") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(listOf("Kg", "Litros", "Unidades")) { u ->
                                FilterChip(selected = unidad == u, onClick = { unidad = u }, label = { Text(u) })
                            }
                        }
                        Button(
                            onClick = {
                                val c = nuevaCant.toDoubleOrNull() ?: 0.0
                                val m = nuevaMin.toDoubleOrNull() ?: 0.0
                                viewModel.guardarInsumo(Insumo(nombre = nuevoNombre, cantidadActual = c, cantidadMinima = m, unidadMedida = unidad)) {
                                    if (it) { nuevoNombre = ""; nuevaCant = ""; mostrarFormularioCrear = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                        ) { Text("Guardar en Nube ☁️") }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val filtrados = insumos.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
                items(filtrados) { insumo ->
                    val color = when {
                        insumo.cantidadActual <= 0 -> Color.Red
                        insumo.cantidadActual <= insumo.cantidadMinima -> Color(0xFFD97706)
                        else -> Color(0xFF10B981)
                    }
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        insumoSeleccionado = insumo
                        editCant = insumo.cantidadActual.toString()
                        editMin = insumo.cantidadMinima.toString()
                    }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(insumo.nombre, fontWeight = FontWeight.Bold)
                                Text("Mínimo: ${insumo.cantidadMinima} ${insumo.unidadMedida}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text("${insumo.cantidadActual} ${insumo.unidadMedida}", fontWeight = FontWeight.Black, color = color)
                        }
                    }
                }
            }
        }
    }

    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null },
            title = { Text("Gestionar Insumo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editCant, onValueChange = { editCant = it }, label = { Text("Cantidad Actual") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = editMin, onValueChange = { editMin = it }, label = { Text("Stock Mínimo") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
            },
            confirmButton = {
                Button(onClick = {
                    val c = editCant.toDoubleOrNull() ?: 0.0
                    val m = editMin.toDoubleOrNull() ?: 0.0
                    viewModel.guardarInsumo(insumoSeleccionado!!.copy(cantidadActual = c, cantidadMinima = m)) { if (it) insumoSeleccionado = null }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.eliminarInsumo(insumoSeleccionado!!.id) { if (it) insumoSeleccionado = null } }) { Text("Eliminar", color = Color.Red) }
            }
        )
    }
}
