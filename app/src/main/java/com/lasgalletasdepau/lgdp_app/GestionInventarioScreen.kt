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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// MODELO DE DATOS PARA LOS INSUMOS (MATERIA PRIMA)
data class Insumo(
    val id: Int,
    val nombre: String,
    val cantidadActual: Double,
    val cantidadMinima: Double, // Alerta si baja de aquí
    val unidadMedida: String,   // "Kg", "Litros", "Unidades"
    val categoria: String       // "Lácteos", "Harinas", "Descartables", etc.
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionInventarioScreen() {
    // LISTA DE INSUMOS DE PRUEBA
    var listaInsumos by remember {
        mutableStateOf(
            listOf(
                Insumo(1, "Harina de Trigo", 25.0, 10.0, "Kg", "Secos"),
                Insumo(2, "Leche Entera Gloria", 4.0, 6.0, "Litros", "Lácteos"),
                Insumo(3, "Chispas de Chocolate", 1.5, 2.0, "Kg", "Secos"),
                Insumo(4, "Vasos para Café 8oz", 120.0, 50.0, "Unidades", "Descartables"),
                Insumo(5, "Fresas Frescas", 0.0, 3.0, "Kg", "Frutas")
            )
        )
    }

    // LISTA DE CATEGORÍAS Y FILTROS
    var categorias by remember { mutableStateOf(listOf("Todos", "Secos", "Lácteos", "Descartables", "Frutas")) }
    var categoriaSeleccionada by remember { mutableStateOf("Todos") }
    var textoBusqueda by remember { mutableStateOf("") }

    // ESTADOS PARA CREAR NUEVO INSUMO
    var mostrarFormularioCrear by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevaCantidad by remember { mutableStateOf("") }
    var nuevaCantidadMinima by remember { mutableStateOf("") }
    var nuevaUnidadMedida by remember { mutableStateOf("Kg") }
    var nuevaCategoriaSeleccionada by remember { mutableStateOf("Secos") }

    // ESTADOS PARA CREAR CATEGORÍA INTERNA
    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var nuevaCategoriaTexto by remember { mutableStateOf("") }

    // ESTADOS PARA GESTIONAR / EDITAR INSUMO EXISTENTE
    var insumoSeleccionado by remember { mutableStateOf<Insumo?>(null) }
    var editNombre by remember { mutableStateOf("") }
    var editCantidad by remember { mutableStateOf("") }
    var editCantidadMinima by remember { mutableStateOf("") }
    var editUnidad by remember { mutableStateOf("Kg") }
    var editCategoria by remember { mutableStateOf("") }
    var mostrarConfirmarEliminar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario de Insumos 📦", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarFormularioCrear = !mostrarFormularioCrear },
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White
            ) {
                Icon(if (mostrarFormularioCrear) Icons.Default.Close else Icons.Default.Add, contentDescription = "Agregar Insumo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {

            // SECCIÓN: FORMULARIO PARA REGISTRAR NUEVO INSUMO
            AnimatedVisibility(visible = mostrarFormularioCrear) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Registrar Materia Prima / Insumo", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D), fontSize = 16.sp)

                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = { Text("Nombre del Insumo (Ej. Azúcar Blanca)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Cantidad Inicial
                            OutlinedTextField(
                                value = nuevaCantidad,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) nuevaCantidad = input
                                },
                                label = { Text("Cant. Inicial") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Alerta Mínima
                            OutlinedTextField(
                                value = nuevaCantidadMinima,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) nuevaCantidadMinima = input
                                },
                                label = { Text("Stock Mínimo") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Selector de Unidad de Medida
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Unidad:", fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                            listOf("Kg", "Litros", "Unidades").forEach { unidad ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = nuevaUnidadMedida == unidad, onClick = { nuevaUnidadMedida = unidad })
                                    Text(unidad, fontSize = 14.sp)
                                }
                            }
                        }

                        // Selector de Categoría
                        Text("Categoría del Insumo:", fontSize = 13.sp, color = Color.Gray)
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
                                val cantDouble = nuevaCantidad.toDoubleOrNull() ?: 0.0
                                val minDouble = nuevaCantidadMinima.toDoubleOrNull() ?: 0.0
                                if (nuevoNombre.isNotBlank()) {
                                    val nuevoId = (listaInsumos.maxOfOrNull { it.id } ?: 0) + 1
                                    listaInsumos = listaInsumos + Insumo(
                                        id = nuevoId,
                                        nombre = nuevoNombre,
                                        cantidadActual = cantDouble,
                                        cantidadMinima = minDouble,
                                        unidadMedida = nuevaUnidadMedida,
                                        categoria = nuevaCategoriaSeleccionada
                                    )
                                    nuevoNombre = ""
                                    nuevaCantidad = ""
                                    nuevaCantidadMinima = ""
                                    mostrarFormularioCrear = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = nuevoNombre.isNotBlank() && nuevaCantidad.isNotBlank() && nuevaCantidadMinima.isNotBlank()
                        ) {
                            Text("Guardar Insumo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // BUSCADOR EN TIEMPO REAL
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar insumo (harina, leche...)", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
                singleLine = true
            )

            // FILTROS HORIZONTALES DE CATEGORÍAS + BOTÓN CREAR
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { mostrarDialogoCategoria = true },
                    modifier = Modifier.background(Color(0xFF1E233D).copy(alpha = 0.1f), RoundedCornerShape(50.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Categoría", tint = Color(0xFF1E233D))
                }
            }

            // LISTADO DE INSUMOS CON INDICADORES TIPO SEMÁFORO
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val insumosFiltrados = listaInsumos.filter {
                    (categoriaSeleccionada == "Todos" || it.categoria == categoriaSeleccionada) &&
                            it.nombre.contains(textoBusqueda, ignoreCase = true)
                }

                items(insumosFiltrados) { insumo ->
                    // Lógica del color del Semáforo
                    val colorSemafórico = when {
                        insumo.cantidadActual <= 0 -> Color(0xFFB91C1C) // Rojo: Agotado
                        insumo.cantidadActual <= insumo.cantidadMinima -> Color(0xFFD97706) // Ámbar: Bajo Mínimo
                        else -> Color(0xFF10B981) // Verde: Óptimo
                    }

                    val estadoTexto = when {
                        insumo.cantidadActual <= 0 -> "🔴 Agotado"
                        insumo.cantidadActual <= insumo.cantidadMinima -> "⚠️ Stock Crítico"
                        else -> "🟢 Estable"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                insumoSeleccionado = insumo
                                editNombre = insumo.nombre
                                editCantidad = insumo.cantidadActual.toString()
                                editCantidadMinima = insumo.cantidadMinima.toString()
                                editUnidad = insumo.unidadMedida
                                editCategoria = insumo.categoria
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                // Círculo Semáforo Visual
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(colorSemafórico, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = insumo.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E233D))
                                    Text(text = "Clasificación: ${insumo.categoria}", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = estadoTexto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colorSemafórico)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${insumo.cantidadActual} ${insumo.unidadMedida}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E233D)
                                )
                                Text(
                                    text = "Min: ${insumo.cantidadMinima} ${insumo.unidadMedida}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO: CREAR NUEVA CATEGORÍA DE INSUMO ---
    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCategoria = false },
            title = { Text("Nueva Categoría de Insumos", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nuevaCategoriaTexto,
                    onValueChange = { nuevaCategoriaTexto = it },
                    label = { Text("Ej. Lácteos, Coberturas, Frutas") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val textoLimpio = nuevaCategoriaTexto.trim()
                        if (textoLimpio.isNotBlank() && !categorias.any { it.equals(textoLimpio, ignoreCase = true) }) {
                            // Cambiado 'categories' por 'categorias' y removida la línea duplicada
                            categorias = categorias + textoLimpio
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

    // --- DIÁLOGO: MODIFICAR INSUMO Y AJUSTAR REABASTECIMIENTO ---
    if (insumoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { insumoSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Detalle de Insumo", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    IconButton(onClick = { mostrarConfirmarEliminar = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFB91C1C))
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editNombre, onValueChange = { editNombre = it }, label = { Text("Nombre del Insumo") })

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editCantidad,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) editCantidad = input
                            },
                            label = { Text("Stock Actual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = editCantidadMinima,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) editCantidadMinima = input
                            },
                            label = { Text("Stock Mínimo") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Kg", "Litros", "Unidades").forEach { unidad ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = editUnidad == unidad, onClick = { editUnidad = unidad })
                                Text(unidad, fontSize = 12.sp)
                            }
                        }
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
                        val cantDouble = editCantidad.toDoubleOrNull() ?: 0.0
                        val minDouble = editCantidadMinima.toDoubleOrNull() ?: 0.0
                        if (editNombre.isNotBlank()) {
                            listaInsumos = listaInsumos.map {
                                if (it.id == insumoSeleccionado?.id) {
                                    it.copy(nombre = editNombre, cantidadActual = cantDouble, cantidadMinima = minDouble, unidadMedida = editUnidad, categoria = editCategoria)
                                } else it
                            }
                            insumoSeleccionado = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    enabled = editNombre.isNotBlank() && editCantidad.isNotBlank() && editCantidadMinima.isNotBlank()
                ) { Text("Guardar Cambios") }
            },
            dismissButton = { TextButton(onClick = { insumoSeleccionado = null }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    // --- DIÁLOGO DE SEGURIDAD: ELIMINAR INSUMO ---
    if (mostrarConfirmarEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEliminar = false },
            title = { Text("¿Eliminar Insumo?", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C)) },
            text = { Text("Al eliminar '${insumoSeleccionado?.nombre}', ya no figurará en tus registros de inventario base.") },
            confirmButton = {
                Button(
                    onClick = {
                        listaInsumos = listaInsumos.filterNot { it.id == insumoSeleccionado?.id }
                        mostrarConfirmarEliminar = false
                        insumoSeleccionado = null
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
fun GestionInventarioScreenPreview() {
    GestionInventarioScreen()
}