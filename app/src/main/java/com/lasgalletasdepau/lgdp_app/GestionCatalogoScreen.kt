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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MODELO EVOLUCIONADO CON CONTROL DE STOCK
data class Producto(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val estaDisponible: Boolean,
    val controlaStock: Boolean, // true = Galletas/Gaseosas, false = Jugos/Café hecho al momento
    val stock: Int // Solo importa si controlaStock es true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionCatalogoScreen() {
    // LISTA DE PRODUCTOS ACTUALIZADA CON LA NUEVA LÓGICA
    var listaProductos by remember {
        mutableStateOf(
            listOf(
                Producto(1, "Galleta de Chocochips", "Galletas", 4.50, true, controlaStock = true, stock = 24),
                Producto(2, "Tarta de limón", "Tartas", 8.50, true, controlaStock = true, stock = 6),
                Producto(3, "Café Americano Frío", "Bebidas", 6.00, true, controlaStock = false, stock = 0),
                Producto(4, "Jugo de Naranja Natural", "Bebidas", 7.50, true, controlaStock = false, stock = 0),
                Producto(5, "Inca Kola Personal", "Bebidas", 3.50, false, controlaStock = true, stock = 0)
            )
        )
    }

    var categorias by remember { mutableStateOf(listOf("Todos", "Galletas", "Tartas", "Bebidas")) }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }

    // ESTADOS CREAR PRODUCTO
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoPrecio by remember { mutableStateOf("") }
    var nuevaCategoriaSeleccionada by remember { mutableStateOf("Galletas") }
    var nuevoControlaStock by remember { mutableStateOf(false) }
    var nuevoStock by remember { mutableStateOf("") }

    // ESTADOS CREAR CATEGORÍA
    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var nuevaCategoriaTexto by remember { mutableStateOf("") }

    // ESTADOS EDITAR PRODUCTO
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editPrecio by remember { mutableStateOf("") }
    var editCategoria by remember { mutableStateOf("") }
    var editControlaStock by remember { mutableStateOf(false) }
    var editStock by remember { mutableStateOf("") }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carta e Inventario 🧁", fontWeight = FontWeight.ExtraBold, color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {

            // FORMULARIO PARA AÑADIR PRODUCTO
            AnimatedVisibility(visible = mostrarFormularioCrear) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Añadir Nuevo Producto", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D), fontSize = 16.sp)

                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = { Text("Nombre del Producto") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = nuevoPrecio,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) nuevoPrecio = input
                            },
                            label = { Text("Precio (S/.)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // SWITCH PARA DECIDIR SI LLEVA INVENTARIO FIJO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("¿Controlar cantidad física?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Actívalo para galletas o gaseosas sueltas. Desactívalo para jugos/cafés del momento.", fontSize = 11.sp, color = Color.Gray, lineHeight = 14.sp)
                            }
                            Switch(checked = nuevoControlaStock, onCheckedChange = { nuevoControlaStock = it })
                        }

                        // CAMPO STOCK CONDICIONAL (Solo aparece si controlaStock es true)
                        AnimatedVisibility(visible = nuevoControlaStock) {
                            OutlinedTextField(
                                value = nuevoStock,
                                onValueChange = { input ->
                                    // Restricción: Solo números enteros
                                    if (input.isEmpty() || input.all { it.isDigit() }) nuevoStock = input
                                },
                                label = { Text("Cantidad disponible en vitrina/almacén") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Text("Categoría:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categorias.filter { it != "Todos" }) { cat ->
                                FilterChip(
                                    selected = nuevaCategoriaSeleccionada == cat,
                                    onClick = { nuevaCategoriaSeleccionada = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val precioDouble = nuevoPrecio.toDoubleOrNull() ?: 0.0
                                val stockInt = if (nuevoControlaStock) (nuevoStock.toIntOrNull() ?: 0) else 0
                                if (nuevoNombre.isNotBlank() && precioDouble > 0.0) {
                                    val nuevoId = (listaProductos.maxOfOrNull { it.id } ?: 0) + 1
                                    listaProductos = listaProductos + Producto(
                                        id = nuevoId,
                                        nombre = nuevoNombre,
                                        categoria = nuevaCategoriaSeleccionada,
                                        precio = precioDouble,
                                        estaDisponible = if (nuevoControlaStock) stockInt > 0 else true,
                                        controlaStock = nuevoControlaStock,
                                        stock = stockInt
                                    )
                                    nuevoNombre = ""
                                    nuevoPrecio = ""
                                    nuevoStock = ""
                                    nuevoControlaStock = false
                                    mostrarFormularioCrear = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = nuevoNombre.isNotBlank() && (nuevoPrecio.toDoubleOrNull() ?: 0.0) > 0.0 && (!nuevoControlaStock || nuevoStock.isNotBlank())
                        ) {
                            Text("Guardar Producto", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FILTROS HORIZONTALES
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categorias) { cat ->
                        FilterChip(
                            selected = cat == categoriaSeleccionada,
                            onClick = { categoriaSeleccionada = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1E233D),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF1E233D)
                            )
                        )
                    }
                }
                IconButton(
                    onClick = { mostrarDialogoCategoria = true },
                    modifier = Modifier.background(Color(0xFF1E233D).copy(alpha = 0.1f), RoundedCornerShape(50.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Categoría", tint = Color(0xFF1E233D))
                }
            }

            // LISTADO DE ÍTEMS
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val productosFiltrados = if (categoriaSeleccionada == "Todos") listaProductos else listaProductos.filter { it.categoria == categoriaSeleccionada }

                items(productosFiltrados) { producto ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            productoSeleccionado = producto
                            editNombre = producto.nombre
                            editPrecio = producto.precio.toString()
                            editCategoria = producto.categoria
                            editControlaStock = producto.controlaStock
                            editStock = producto.stock.toString()
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = producto.nombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (producto.estaDisponible) Color(0xFF1E233D) else Color.Gray
                                )
                                Text(text = "Categoría: ${producto.categoria}", fontSize = 12.sp, color = Color.Gray)

                                // INDICADOR VISUAL DE STOCK DINÁMICO
                                if (producto.controlaStock) {
                                    Text(
                                        text = if (producto.stock > 0) "📦 Cantidad: ${producto.stock} unds." else "⚠️ ¡Sin Stock Físico!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (producto.stock > 5) Color(0xFF475569) else if (producto.stock > 0) Color(0xFFD97706) else Color(0xFFB91C1C)
                                    )
                                } else {
                                    Text(text = "☕ Preparado al momento", fontSize = 12.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = String.format("S/. %.2f", producto.precio), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (producto.estaDisponible) Color(0xFF1E233D) else Color.Gray)
                            }

                            // SWITCH INTERACTIVO
                            Column(horizontalAlignment = Alignment.End) {
                                Switch(
                                    checked = producto.estaDisponible,
                                    onCheckedChange = { nuevoEstado ->
                                        listaProductos = listaProductos.map {
                                            if (it.id == producto.id) {
                                                // Si controla stock y lo marcan disponible manual, le ponemos 1 por defecto para que no se bloquee
                                                val nuevoStockCalculado = if (producto.controlaStock && nuevoEstado && producto.stock == 0) 1 else producto.stock
                                                it.copy(estaDisponible = nuevoEstado, stock = nuevoStockCalculado)
                                            } else it
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981))
                                )
                                Text(
                                    text = if (producto.estaDisponible) "Disponible" else "Agotado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (producto.estaDisponible) Color(0xFF10B981) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO NUEVA CATEGORÍA
    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCategoria = false },
            title = { Text("Nueva Categoría", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(value = nuevaCategoriaTexto, onValueChange = { nuevaCategoriaTexto = it }, label = { Text("Ej. Empanadas, Cafetería") }, singleLine = true)
            },
            confirmButton = {
                Button(
                    onClick = {
                        val formateada = nuevaCategoriaTexto.trim()
                        if (formateada.isNotBlank() && !categorias.any { it.equals(formateada, ignoreCase = true) }) {
                            categorias = categorias + formateada
                            nuevaCategoriaTexto = ""
                            mostrarDialogoCategoria = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) { Text("Añadir") }
            },
            dismissButton = { TextButton(onClick = { mostrarDialogoCategoria = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    // DIÁLOGO EDITAR / MODIFICAR PRODUCTO Y STOCK
    if (productoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { productoSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Modificar Ítem", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    IconButton(onClick = { mostrarConfirmarEliminar = true }) { Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFB91C1C)) }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre del Producto") })
                    OutlinedTextField(
                        value = editPrecio,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) editPrecio = input
                        },
                        label = { Text("Precio (S/.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("¿Controlar cantidad física?", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Switch(checked = editControlaStock, onCheckedChange = { editControlaStock = it })
                    }

                    AnimatedVisibility(visible = editControlaStock) {
                        OutlinedTextField(
                            value = editStock,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) editStock = input
                            },
                            label = { Text("Cantidad actual en Almacén") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Text("Categoría:", fontSize = 12.sp, color = Color.Gray)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categorias.filter { it != "Todos" }) { cat ->
                            FilterChip(selected = editCategoria == cat, onClick = { editCategoria = cat }, label = { Text(cat) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val precioDouble = editPrecio.toDoubleOrNull() ?: 0.0
                        val stockInt = if (editControlaStock) (editStock.toIntOrNull() ?: 0) else 0
                        if (editNombre.isNotBlank() && precioDouble > 0.0) {
                            listaProductos = listaProductos.map {
                                if (it.id == productoSeleccionado?.id) {
                                    it.copy(
                                        nombre = editNombre,
                                        precio = precioDouble,
                                        categoria = editCategoria,
                                        controlaStock = editControlaStock,
                                        stock = stockInt,
                                        // Si el stock físico es 0 o menor, se marca automáticamente como no disponible
                                        estaDisponible = if (editControlaStock) stockInt > 0 else it.estaDisponible
                                    )
                                } else it
                            }
                            productoSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    enabled = editNombre.isNotBlank() && (editPrecio.toDoubleOrNull() ?: 0.0) > 0.0 && (!editControlaStock || editStock.isNotBlank())
                ) { Text("Guardar Cambios") }
            },
            dismissButton = { TextButton(onClick = { productoSeleccionado = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    // DIÁLOGO CONFIRMAR ELIMINAR
    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("¿Eliminar del catálogo?", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = { Text("Esto removerá permanentemente el producto '${productoSeleccionado?.nombre}' de la lista.") },
            confirmButton = {
                Button(
                    onClick = {
                        listaProductos = listaProductos.filterNot { it.id == productoSeleccionado?.id }
                        mostrarConfirmarEliminar = false
                        productoSeleccionado = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C))
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmarEliminar = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GestionCatalogoScreenPreview() {
    GestionCatalogoScreen()
}