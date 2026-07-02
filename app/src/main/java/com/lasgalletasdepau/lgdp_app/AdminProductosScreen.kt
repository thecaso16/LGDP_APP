package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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

// Estructura de datos para los productos en el panel de administración
data class ProductoAdmin(
    val id: Int,
    val nombre: String,
    val categoria: String,
    val precio: Double,
    val stock: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductosScreen() {
    // --- ESTADOS DE LA PANTALLA ---
    // Lista simulada de productos iniciales
    var listaProductos by remember {
        mutableStateOf(
            listOf(
                ProductoAdmin(1, "Galleta Chocochips", "Galletas", 4.50, 45),
                ProductoAdmin(2, "Galleta Avena y Pasas", "Galletas", 4.00, 20),
                ProductoAdmin(3, "Cheesecake de Fresa", "Postres", 12.00, 4),
                ProductoAdmin(4, "Café Americano 8oz", "Bebidas", 6.50, 80),
                ProductoAdmin(5, "Galleta Red Velvet", "Galletas", 5.00, 5)
            )
        )
    }

    var textoBusqueda by remember { mutableStateOf("") }

    // Control de diálogos
    var mostrarDialogoFormulario by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<ProductoAdmin?>(null) }

    // Filtrar la lista según lo que escriba el administrador
    val productosFiltrados = listaProductos.filter {
        it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.categoria.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Catálogo", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            // Botón flotante para agregar un producto desde cero
            FloatingActionButton(
                onClick = {
                    productoAEditar = null // Aseguramos que el formulario entre en modo "Crear"
                    mostrarDialogoFormulario = true
                },
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            // --- BARRA DE BÚSQUEDA ---
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o categoría...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E233D),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,   // <--- Cambiado aquí
                    unfocusedContainerColor = Color.White // <--- Cambiado aquí
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- LISTADO DE PRODUCTOS ---
            if (productosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos 🧐", color = Color.Gray, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productosFiltrados) { producto ->
                        TarjetaProductoAdmin(
                            producto = producto,
                            onEditarClick = {
                                productoAEditar = producto
                                mostrarDialogoFormulario = true
                            }
                        )
                    }
                }
            }
        }
    }

    // --- FORMULARIO FLOTANTE (AGREGAR / EDITAR) ---
    if (mostrarDialogoFormulario) {
        FormularioProductoDialog(
            producto = productoAEditar,
            onDismiss = { mostrarDialogoFormulario = false },
            onGuardar = { nombre, categoria, precio, stock ->
                if (productoAEditar == null) {
                    // Modo: Agregar nuevo producto
                    val nuevoId = (listaProductos.maxOfOrNull { it.id } ?: 0) + 1
                    val nuevoProducto = ProductoAdmin(nuevoId, nombre, categoria, precio, stock)
                    listaProductos = listaProductos + nuevoProducto
                } else {
                    // Modo: Editar existente
                    listaProductos = listaProductos.map {
                        if (it.id == productoAEditar!!.id) {
                            it.copy(nombre = nombre, categoria = categoria, precio = precio, stock = stock)
                        } else {
                            it
                        }
                    }
                }
                mostrarDialogoFormulario = false
            }
        )
    }
}

@Composable
fun TarjetaProductoAdmin(
    producto: ProductoAdmin,
    onEditarClick: () -> Unit
) {
    // Alerta visual si el stock es bajo (5 unidades o menos)
    val esStockCritico = producto.stock <= 5
    val colorStock = if (esStockCritico) Color(0xFFEF4444) else Color(0xFF475569)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E233D)
                )
                Text(
                    text = producto.categoria,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = String.format("Precio: S/. %.2f", producto.precio),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981) // Verde para el dinero
                    )
                    Text(
                        text = "Stock: ${producto.stock} ud.",
                        fontSize = 13.sp,
                        fontWeight = if (esStockCritico) FontWeight.Bold else FontWeight.Normal,
                        color = colorStock
                    )
                }
            }

            // Botón de acción rápida para abrir edición
            IconButton(onClick = onEditarClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF3B82F6))
            }
        }
    }
}

@Composable
fun FormularioProductoDialog(
    producto: ProductoAdmin?,
    onDismiss: () -> Unit,
    onGuardar: (String, String, Double, Int) -> Unit
) {
    // Si viene un producto cargamos sus datos (Modo Editar), si es null vacíos (Modo Crear)
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "Galletas") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var stock by remember { mutableStateOf(producto?.stock?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = if (producto == null) "Nuevo Producto 🍪" else "Editar Producto ✏️",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1E233D)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría (Galletas, Bebidas...)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio (S/.)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock Inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val stockInt = stock.toIntOrNull() ?: 0
                    if (nombre.isNotBlank() && categoria.isNotBlank()) {
                        onGuardar(nombre, categoria, precioDouble, stockInt)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AdminProductosScreenPreview() {
    AdminProductosScreen()
}