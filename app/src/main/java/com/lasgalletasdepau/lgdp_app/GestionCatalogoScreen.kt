package com.lasgalletasdepau.lgdp_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import com.lasgalletasdepau.lgdp_app.ui.admin.CategoriasViewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.GestionCatalogoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCatalogoScreen(
    viewModel: GestionCatalogoViewModel = viewModel(),
    catViewModel: CategoriasViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val categorias by catViewModel.categorias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var filtroCategoria by remember { mutableStateOf("Todos") }

    // ESTADOS CREAR
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoPrecio by remember { mutableStateOf("") }
    var nuevaCatId by remember { mutableStateOf("") }
    var nuevoStock by remember { mutableStateOf("") }
    var controlaStock by remember { mutableStateOf(false) }

    // ESTADOS EDITAR
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editPrecio by remember { mutableStateOf("") }
    var editStock by remember { mutableStateOf("") }
    var mostrarDialogoCat by remember { mutableStateOf(false) }
    var nombreNuevaCat by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta e Inventario 🧁", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Barra de Categorías (Filtro)
            LazyRow(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = filtroCategoria == "Todos", onClick = { filtroCategoria = "Todos" }, label = { Text("Todos") })
                }
                items(categorias) { cat ->
                    FilterChip(selected = filtroCategoria == cat.nombre, onClick = { filtroCategoria = cat.nombre }, label = { Text(cat.nombre) })
                }
                item {
                    IconButton(onClick = { mostrarDialogoCat = true }) { Icon(Icons.Default.Add, contentDescription = null) }
                }
            }

            AnimatedVisibility(visible = mostrarFormularioCrear) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = nuevoPrecio, onValueChange = { nuevoPrecio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        
                        Text("Categoría:", fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(categorias) { cat ->
                                FilterChip(selected = nuevaCatId == cat.nombre, onClick = { nuevaCatId = cat.nombre }, label = { Text(cat.nombre) })
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Controlar Stock")
                            Switch(checked = controlaStock, onCheckedChange = { controlaStock = it })
                        }

                        if (controlaStock) {
                            OutlinedTextField(value = nuevoStock, onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) nuevoStock = it }, label = { Text("Stock inicial") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }

                        Button(
                            onClick = {
                                val p = nuevoPrecio.toDoubleOrNull() ?: 0.0
                                val s = nuevoStock.toIntOrNull() ?: 0
                                viewModel.guardarProducto(Producto(nombre = nuevoNombre, precio = p, categoriaId = nuevaCatId, stock = s, controlaStock = controlaStock)) {
                                    if (it) { nuevoNombre = ""; nuevoPrecio = ""; mostrarFormularioCrear = false }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                        ) { Text("Agregar a Firebase") }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filtrados = if (filtroCategoria == "Todos") productos else productos.filter { it.categoriaId == filtroCategoria }
                items(filtrados) { prod ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        productoSeleccionado = prod
                        editNombre = prod.nombre
                        editPrecio = prod.precio.toString()
                        editStock = prod.stock.toString()
                    }, colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.nombre, fontWeight = FontWeight.Bold)
                                Text("S/ ${String.format("%.2f", prod.precio)} | ${prod.categoriaId}", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (prod.controlaStock) {
                                Text("Stock: ${prod.stock}", color = if (prod.stock < 5) Color.Red else Color.DarkGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogos para Categorías
    if (mostrarDialogoCat) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCat = false },
            title = { Text("Nueva Categoría") },
            text = { OutlinedTextField(value = nombreNuevaCat, onValueChange = { nombreNuevaCat = it }, label = { Text("Nombre") }) },
            confirmButton = { Button(onClick = { catViewModel.agregarCategoria(nombreNuevaCat); mostrarDialogoCat = false; nombreNuevaCat = "" }) { Text("Añadir") } }
        )
    }

    if (productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { productoSeleccionado = null },
            title = { Text("Editar Producto") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") })
                    OutlinedTextField(value = editPrecio, onValueChange = { editPrecio = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    if (productoSeleccionado!!.controlaStock) {
                        OutlinedTextField(value = editStock, onValueChange = { editStock = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = editPrecio.toDoubleOrNull() ?: 0.0
                    val s = editStock.toIntOrNull() ?: 0
                    val original = productoSeleccionado!!
                    viewModel.guardarProducto(original.copy(nombre = editNombre, precio = p, stock = s)) {
                        if (it) productoSeleccionado = null
                    }
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    productoSeleccionado?.id?.let { id ->
                        viewModel.eliminarProducto(id) { if (it) productoSeleccionado = null }
                    }
                }) { Text("Eliminar", color = Color.Red) }
            }
        )
    }
}
