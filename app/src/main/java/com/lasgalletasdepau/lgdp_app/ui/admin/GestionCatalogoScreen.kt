package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import com.lasgalletasdepau.lgdp_app.domain.model.ProductoInsumo

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
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    var mostrarDialogoVinculos by remember { mutableStateOf<Producto?>(null) }
    var vinculosActuales by remember { mutableStateOf<List<ProductoInsumo>>(emptyList()) }
    var insumoParaVincularId by remember { mutableStateOf("") }
    var cantidadVinculo by remember { mutableStateOf("") }
    val insumosDisponibles by viewModel.insumosDisponibles.collectAsState()

    var mostrarDialogoCat by remember { mutableStateOf(false) }
    var nombreNuevaCat by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), color = MaterialTheme.colorScheme.primary)
                }
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Gestión de Catálogo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Organice su carta de productos y categorías:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(categorias) { cat ->
                        FilterChip(
                            selected = filtroCategoriaId == cat.id, 
                            onClick = { filtroCategoriaId = cat.id }, 
                            label = { Text(cat.nombre ?: "") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    item {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            onClick = { mostrarDialogoCat = true },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.primary
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
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Nuevo Producto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            
                            OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, label = { Text("Nombre del producto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoDescripcion, onValueChange = { nuevoDescripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = RoundedCornerShape(12.dp))
                            OutlinedTextField(value = nuevoPrecio, onValueChange = { nuevoPrecio = it }, label = { Text("Precio (S/.)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp))
                            
                            Column {
                                Text("Categoría:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(top = 4.dp)) {
                                    items(categorias) { cat ->
                                        FilterChip(
                                            selected = nuevaCatId == cat.id, 
                                            onClick = { nuevaCatId = cat.id }, 
                                            label = { Text(cat.nombre ?: "") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
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
                                    Text("Controlar stock", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.width(8.dp))
                                    Switch(checked = controlaStock, onCheckedChange = { controlaStock = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Recomendado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
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
                                            id = "",
                                            nombre = nuevoNombre, 
                                            descripcion = nuevoDescripcion,
                                            precio = p, 
                                            categoriaId = nuevaCatId, 
                                            stock = s, 
                                            controlaStock = controlaStock,
                                            estaDisponible = true,
                                            imagen = null,
                                            recomendado = nuevoRecomendado,
                                            activo = true,
                                            ultimaActualizacion = null
                                        )
                                    ) {
                                        if (it) { 
                                            nuevoNombre = ""; nuevoDescripcion = ""; nuevoPrecio = ""; nuevaCatId = ""; nuevoStock = ""; controlaStock = false; nuevoRecomendado = false; mostrarFormularioCrear = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                        editDescripcion = prod.descripcion ?: ""
                        editPrecio = prod.precio.toString()
                        editStock = prod.stock.toString()
                        editCatId = prod.categoriaId ?: ""
                        editControlaStock = prod.controlaStock
                        editDisponible = prod.estaDisponible
                        editRecomendado = prod.recomendado
                    },
                    colors = CardDefaults.cardColors(containerColor = if(prod.estaDisponible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            val catNombre = categorias.find { it.id == prod.categoriaId }?.nombre ?: prod.categoriaId
                            val locale = LocalConfiguration.current.locales[0]
                            Text("S/ ${String.format(locale, "%.2f", prod.precio)} | $catNombre", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (prod.controlaStock) {
                            Surface(
                                color = if (prod.stock < 5) Color(0xFFFFEBEE).copy(alpha = if(isSystemInDarkTheme()) 0.2f else 1f) else Color(0xFFE8F5E9).copy(alpha = if(isSystemInDarkTheme()) 0.2f else 1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Stock: ${prod.stock}", 
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (prod.stock < 5) Color.Red else (if(isSystemInDarkTheme()) Color(0xFFA5D6A7) else Color(0xFF2E7D32)), 
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
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
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Nueva Categoría", color = MaterialTheme.colorScheme.onSurface) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Añadir", color = Color.White) } 
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCat = false }) { Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    if (productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { productoSeleccionado = null },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Editar Producto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editDescripcion, onValueChange = { editDescripcion = it }, label = { Text("Descripción") }, minLines = 2, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = editPrecio, onValueChange = { editPrecio = it }, label = { Text("Precio") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    
                    Column {
                        Text("Categoría:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(top = 4.dp)) {
                            items(categorias) { cat ->
                                FilterChip(
                                    selected = editCatId == cat.id, 
                                    onClick = { editCatId = cat.id }, 
                                    label = { Text(cat.nombre ?: "") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
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
                            Text("Controlar stock", color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = editControlaStock, onCheckedChange = { editControlaStock = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Disponible", color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = editDisponible, onCheckedChange = { editDisponible = it })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recomendado", color = MaterialTheme.colorScheme.onSurface)
                            Switch(checked = editRecomendado, onCheckedChange = { editRecomendado = it })
                        }
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Actualizar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = true }) { Text("Eliminar", color = Color.Red) }
            }
        )
    }

    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("¿Eliminar producto?") },
            text = { Text("¿Está seguro de que desea eliminar este producto del catálogo?") },
            confirmButton = {
                Button(
                    onClick = {
                        productoSeleccionado?.id?.let { id ->
                            viewModel.eliminarProductoLogico(id) {
                                if (it) {
                                    productoSeleccionado = null
                                    mostrarConfirmarEliminar = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar") }
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Vincular", color = Color.White) }
                }
            },
            confirmButton = { TextButton(onClick = { mostrarDialogoVinculos = null }) { Text("Cerrar") } }
        )
    }
}
