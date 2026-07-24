package com.lasgalletasdepau.lgdp_app.ui.admin

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
import androidx.compose.ui.platform.LocalConfiguration
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoInsumoEntity
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

    var filtroCategoriaId by remember { mutableStateOf("Todos") }

    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoDescripcion by remember { mutableStateOf("") }
    var nuevoPrecio by remember { mutableStateOf("") }
    var nuevaCatId by remember { mutableStateOf("") }
    var nuevoStock by remember { mutableStateOf("") }
    var controlaStock by remember { mutableStateOf(false) }
    var nuevoRecomendado by remember { mutableStateOf(false) }

    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editDescripcion by remember { mutableStateOf("") }
    var editPrecio by remember { mutableStateOf("") }
    var editStock by remember { mutableStateOf("") }
    var editCatId by remember { mutableStateOf("") }
    var editControlaStock by remember { mutableStateOf(false) }
    var editDisponible by remember { mutableStateOf(true) }
    var editRecomendado by remember { mutableStateOf(false) }

    var mostrarDialogoVinculos by remember { mutableStateOf<Producto?>(null) }
    var vinculosActuales by remember { mutableStateOf<List<ProductoInsumoEntity>>(emptyList()) }
    var insumoParaVincularId by remember { mutableStateOf("") }
    var cantidadVinculo by remember { mutableStateOf("") }
    val insumosDisponibles by viewModel.insumosDisponibles.collectAsState()

    var mostrarDialogoCat by remember { mutableStateOf(false) }
    var nombreNuevaCat by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), color = Color(0xFF1E233D))
                }
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Gestión de Catálogo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E233D)
                    )
                    Text(
                        text = "Organice su carta de productos y categorías:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
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
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            onClick = { mostrarDialogoCat = true },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E233D).copy(alpha = 0.08f),
                            contentColor = Color(0xFF1E233D)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Nueva", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = mostrarFormularioCrear) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Nuevo Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoDescripcion, onValueChange = { nuevoDescripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoPrecio, onValueChange = { nuevoPrecio = it }, label = { Text("Precio (S/.)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp))
                            
                            Column {
                                Text("Categoría:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(top = 4.dp)) {
                                    items(categorias) { cat ->
                                        FilterChip(
                                            selected = nuevaCatId == cat.id, 
                                            onClick = { nuevaCatId = cat.id }, 
                                            label = { Text(cat.nombre) }
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Controlar stock", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.width(8.dp))
                                    Switch(checked = controlaStock, onCheckedChange = { controlaStock = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Recomendado", style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.width(8.dp))
                                    Switch(checked = nuevoRecomendado, onCheckedChange = { nuevoRecomendado = it })
                                }
                            }

                            if (controlaStock) {
                                OutlinedTextField(
                                    value = nuevoStock, 
                                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) nuevoStock = it }, 
                                    label = { Text("Stock inicial") }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
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
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                                enabled = nuevoNombre.isNotBlank() && nuevaCatId.isNotBlank(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Guardar Producto") }
                        }
                    }
                }
            }

            val filtrados = if (filtroCategoriaId == "Todos") productos else productos.filter { it.categoriaId == filtroCategoriaId }
            items(filtrados) { prod ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable {
                        productoSeleccionado = prod
                        editNombre = prod.nombre
                        editDescripcion = prod.descripcion
                        editPrecio = prod.precio.toString()
                        editStock = prod.stock.toString()
                        editCatId = prod.categoriaId
                        editControlaStock = prod.controlaStock
                        editDisponible = prod.estaDisponible
                        editRecomendado = prod.recomendado
                    },
                    colors = CardDefaults.cardColors(containerColor = if(prod.estaDisponible) Color.White else Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            val catNombre = categorias.find { it.id == prod.categoriaId }?.nombre ?: prod.categoriaId
                            val locale = LocalConfiguration.current.locales[0]
                            Text("S/ ${String.format(locale, "%.2f", prod.precio)} | $catNombre", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        if (prod.controlaStock) {
                            Surface(
                                color = if (prod.stock < 5) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Stock: ${prod.stock}", 
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (prod.stock < 5) Color.Red else Color(0xFF2E7D32), 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarFormularioCrear = !mostrarFormularioCrear },
            containerColor = Color(0xFF1E233D),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(if (mostrarFormularioCrear) Icons.Default.Close else Icons.Default.Add, contentDescription = null)
        }
    }

    if (mostrarDialogoCat) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCat = false },
            title = { Text("Nueva Categoría") },
            text = { 
                OutlinedTextField(
                    value = nombreNuevaCat, 
                    onValueChange = { nombreNuevaCat = it }, 
                    label = { Text("Nombre de la categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) 
            },
            confirmButton = { 
                Button(
                    onClick = { 
                        catViewModel.agregarCategoria(nombreNuevaCat)
                        mostrarDialogoCat = false
                        nombreNuevaCat = "" 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) { Text("Añadir") } 
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCat = false }) { Text("Cancelar") }
            }
        )
    }

    if (productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { productoSeleccionado = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Editar Producto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editDescripcion, onValueChange = { editDescripcion = it }, label = { Text("Descripción") }, minLines = 2, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editPrecio, onValueChange = { editPrecio = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    
                    Column {
                        Text("Categoría:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(top = 4.dp)) {
                            items(categorias) { cat ->
                                FilterChip(
                                    selected = editCatId == cat.id, 
                                    onClick = { editCatId = cat.id }, 
                                    label = { Text(cat.nombre) }
                                )
                            }
                        }
                    }

                    if (editControlaStock) {
                        OutlinedTextField(
                            value = editStock, 
                            onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) editStock = it }, 
                            label = { Text("Stock disponible") }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Controlar stock")
                            Switch(checked = editControlaStock, onCheckedChange = { editControlaStock = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Disponible")
                            Switch(checked = editDisponible, onCheckedChange = { editDisponible = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recomendado")
                            Switch(checked = editRecomendado, onCheckedChange = { editRecomendado = it })
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.obtenerInsumosRelacionados(productoSeleccionado!!.id) {
                                vinculosActuales = it
                                mostrarDialogoVinculos = productoSeleccionado
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Configurar Ingredientes")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPrecio.toDoubleOrNull() ?: 0.0
                        val s = editStock.toIntOrNull() ?: 0
                        viewModel.guardarProducto(
                            productoSeleccionado!!.copy(
                                nombre = editNombre, 
                                descripcion = editDescripcion,
                                precio = p, 
                                stock = s,
                                controlaStock = editControlaStock,
                                categoriaId = editCatId,
                                estaDisponible = editDisponible,
                                recomendado = editRecomendado
                            )
                        ) {
                            if (it) productoSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) { Text("Actualizar") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    productoSeleccionado?.id?.let { id ->
                        viewModel.eliminarProductoLogico(id) { if (it) productoSeleccionado = null }
                    }
                }) { Text("Eliminar", color = Color.Red) }
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
                        label = { Text("Cantidad necesaria") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                        enabled = insumoParaVincularId.isNotBlank() && cantidadVinculo.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                    ) { Text("Vincular") }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDialogoVinculos = null }) { Text("Cerrar") } }
        )
    }
}
