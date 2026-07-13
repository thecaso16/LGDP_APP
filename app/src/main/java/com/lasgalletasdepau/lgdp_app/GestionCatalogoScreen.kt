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
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.platform.LocalConfiguration
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import com.lasgalletasdepau.lgdp_app.ui.admin.CategoriasViewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.GestionCatalogoViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCatalogoScreen(
    viewModel: GestionCatalogoViewModel = viewModel(),
    catViewModel: CategoriasViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val productos by viewModel.productos.collectAsState()
    val categorias by catViewModel.categorias.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Filtro por ID de categoría
    var filtroCategoriaId by remember { mutableStateOf("Todos") }

    // ESTADOS CREAR
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoDescripcion by remember { mutableStateOf("") }
    var nuevoPrecio by remember { mutableStateOf("") }
    var nuevaCatId by remember { mutableStateOf("") }
    var nuevoStock by remember { mutableStateOf("") }
    var controlaStock by remember { mutableStateOf(false) }
    var nuevoRecomendado by remember { mutableStateOf(false) }

    // ESTADOS EDITAR
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editDescripcion by remember { mutableStateOf("") }
    var editPrecio by remember { mutableStateOf("") }
    var editStock by remember { mutableStateOf("") }
    var editCatId by remember { mutableStateOf("") }
    var editDisponible by remember { mutableStateOf(true) }
    var editRecomendado by remember { mutableStateOf(false) }

    // ESTADOS VÍNCULO INSUMOS
    var mostrarDialogoVinculos by remember { mutableStateOf<Producto?>(null) }
    var vinculosActuales by remember { mutableStateOf<List<com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoInsumoEntity>>(emptyList()) }
    var insumoParaVincularId by remember { mutableStateOf("") }
    var cantidadVinculo by remember { mutableStateOf("") }
    val insumosDisponibles by viewModel.insumosDisponibles.collectAsState()

    var mostrarDialogoCat by remember { mutableStateOf(false) }
    var nombreNuevaCat by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta e Inventario 🧁", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                },
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
                    FilterChip(
                        selected = filtroCategoriaId == "Todos", 
                        onClick = { filtroCategoriaId = "Todos" }, 
                        label = { Text("Todos") }
                    )
                }
                items(categorias) { cat ->
                    FilterChip(
                        selected = filtroCategoriaId == cat.id, 
                        onClick = { filtroCategoriaId = cat.id }, 
                        label = { Text(cat.nombre) }
                    )
                }
                item {
                    IconButton(onClick = { mostrarDialogoCat = true }) { Icon(Icons.Default.Add, contentDescription = null) }
                }
            }

            AnimatedVisibility(visible = mostrarFormularioCrear) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nuevo Producto en Firebase", fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = nuevoDescripcion, onValueChange = { nuevoDescripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        OutlinedTextField(value = nuevoPrecio, onValueChange = { nuevoPrecio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        
                        Text("Categoría:", fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(categorias) { cat ->
                                FilterChip(
                                    selected = nuevaCatId == cat.id, 
                                    onClick = { nuevaCatId = cat.id }, 
                                    label = { Text(cat.nombre) }
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Controlar Stock")
                            Switch(checked = controlaStock, onCheckedChange = { controlaStock = it })
                            Spacer(Modifier.width(16.dp))
                            Text("Recomendado ⭐")
                            Switch(checked = nuevoRecomendado, onCheckedChange = { nuevoRecomendado = it })
                        }

                        if (controlaStock) {
                            OutlinedTextField(value = nuevoStock, onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) nuevoStock = it }, label = { Text("Stock inicial") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        }

                        Button(
                            onClick = {
                                val p = nuevoPrecio.toDoubleOrNull() ?: 0.0
                                val s = nuevoStock.toIntOrNull() ?: 0
                                viewModel.guardarProducto(
                                    Producto(
                                        nombre = nuevoNombre, 
                                        descripcion = nuevoDescripcion,
                                        precio = p, 
                                        categoriaId = nuevaCatId, 
                                        stock = s, 
                                        controlaStock = controlaStock,
                                        recomendado = nuevoRecomendado,
                                        activo = true
                                    )
                                ) {
                                    if (it) { 
                                        nuevoNombre = ""; nuevoDescripcion = ""; nuevoPrecio = ""; nuevaCatId = ""; nuevoStock = ""; controlaStock = false; nuevoRecomendado = false; mostrarFormularioCrear = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                            enabled = nuevoNombre.isNotBlank() && nuevaCatId.isNotBlank()
                        ) { Text("Guardar en Nube ☁️") }
                    }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val filtrados = if (filtroCategoriaId == "Todos") productos else productos.filter { it.categoriaId == filtroCategoriaId }
                
                items(filtrados) { prod ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        productoSeleccionado = prod
                        editNombre = prod.nombre
                        editDescripcion = prod.descripcion
                        editPrecio = prod.precio.toString()
                        editStock = prod.stock.toString()
                        editCatId = prod.categoriaId
                        editDisponible = prod.estaDisponible
                        editRecomendado = prod.recomendado
                    }, colors = CardDefaults.cardColors(containerColor = if(prod.estaDisponible) Color.White else Color(0xFFF1F1F1))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.nombre, fontWeight = FontWeight.Bold)
                                // Mostrar nombre de categoría en lugar de ID
                                val catNombre = categorias.find { it.id == prod.categoriaId }?.nombre ?: prod.categoriaId
                                val locale = LocalConfiguration.current.locales[0]
                                Text("S/ ${String.format(locale, "%.2f", prod.precio)} | $catNombre", fontSize = 12.sp, color = Color.Gray)
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

    // Diálogo para Categorías
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
                    OutlinedTextField(value = editDescripcion, onValueChange = { editDescripcion = it }, label = { Text("Descripción") }, minLines = 2)
                    OutlinedTextField(value = editPrecio, onValueChange = { editPrecio = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    
                    Text("Categoría:", fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(categorias) { cat ->
                            FilterChip(
                                selected = editCatId == cat.id, 
                                onClick = { editCatId = cat.id }, 
                                label = { Text(cat.nombre) }
                            )
                        }
                    }

                    if (productoSeleccionado!!.controlaStock) {
                        OutlinedTextField(value = editStock, onValueChange = { editStock = it }, label = { Text("Stock") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Disponible")
                        Switch(checked = editDisponible, onCheckedChange = { editDisponible = it })
                        Spacer(Modifier.width(16.dp))
                        Text("Recomendado ⭐")
                        Switch(checked = editRecomendado, onCheckedChange = { editRecomendado = it })
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.obtenerInsumosRelacionados(productoSeleccionado!!.id) {
                                vinculosActuales = it
                                mostrarDialogoVinculos = productoSeleccionado
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                    ) {
                        Text("Configurar Ingredientes (Insumos) ⚖️")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val p = editPrecio.toDoubleOrNull() ?: 0.0
                    val s = editStock.toIntOrNull() ?: 0
                    viewModel.guardarProducto(
                        productoSeleccionado!!.copy(
                            nombre = editNombre, 
                            descripcion = editDescripcion,
                            precio = p, 
                            stock = s,
                            categoriaId = editCatId,
                            estaDisponible = editDisponible,
                            recomendado = editRecomendado
                        )
                    ) {
                        if (it) productoSeleccionado = null
                    }
                }) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    productoSeleccionado?.id?.let { id ->
                        viewModel.eliminarProductoLogico(id) { if (it) productoSeleccionado = null }
                    }
                }) { Text("Eliminar (Ocultar)", color = Color.Red) }
            }
        )
    }

    if (mostrarDialogoVinculos != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoVinculos = null },
            title = { Text("Receta: ${mostrarDialogoVinculos!!.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingredientes actuales:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                     vinculosActuales.forEach { vin ->
                         val nombreInsumo = insumosDisponibles.find { it.id == vin.insumoId }?.nombre ?: vin.insumoId
                         Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                             Text("$nombreInsumo: ${vin.cantidadRequerida}", fontSize = 13.sp)
                             IconButton(onClick = {
                                 viewModel.eliminarVinculoInsumo(mostrarDialogoVinculos!!.id, vin.insumoId) {
                                     if (it) viewModel.obtenerInsumosRelacionados(mostrarDialogoVinculos!!.id) { vinculosActuales = it }
                                 }
                             }) { Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(18.dp)) }
                         }
                     }

                    HorizontalDivider()
                    Text("Agregar Insumo:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Selector simple de insumos
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(insumosDisponibles.find { it.id == insumoParaVincularId }?.nombre ?: "Seleccionar Insumo")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            insumosDisponibles.forEach { insumo ->
                                DropdownMenuItem(
                                    text = { Text(insumo.nombre) },
                                    onClick = { insumoParaVincularId = insumo.id; expanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cantidadVinculo,
                        onValueChange = { cantidadVinculo = it },
                        label = { Text("Cantidad necesaria (ej: 0.05)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val cant = cantidadVinculo.toDoubleOrNull() ?: 0.0
                            if (insumoParaVincularId.isNotBlank() && cant > 0) {
                                viewModel.guardarVinculoInsumo(mostrarDialogoVinculos!!.id, insumoParaVincularId, cant) {
                                    if (it) {
                                        cantidadVinculo = ""
                                        viewModel.obtenerInsumosRelacionados(mostrarDialogoVinculos!!.id) { vinculosActuales = it }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = insumoParaVincularId.isNotBlank() && cantidadVinculo.isNotBlank()
                    ) { Text("Vincular") }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDialogoVinculos = null }) { Text("Cerrar") } }
        )
    }
}
