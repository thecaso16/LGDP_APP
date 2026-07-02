package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Estructuras de datos para el menú y el carrito
data class ProductoCatalogo(val nombre: String, val precio: Double, val categoria: String, val stock: Int)
data class ElementoCarrito(val producto: String, var cantidad: Int, val precioUnitario: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(onBackToSalon: () -> Unit = {}) {
    // 1. CONTROL DE LA PESTAÑA PRINCIPAL (0: Menú de Productos, 1: Ver Orden)
    var pestanaActiva by remember { mutableStateOf(0) }

    // Estado para controlar el Método de Pago seleccionado
    var metodoPagoSeleccionado by remember { mutableStateOf("Efectivo 💵") }
    val metodosPago = listOf("Efectivo 💵", "Yape/Plin 📱", "Izipay 💳")

    // Diálogo de seguridad
    var mostrarConfirmarEnvio by remember { mutableStateOf(false) }

    // Datos automáticos de la comanda
    val fechaHoraActual = remember {
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
    }
    var notasCocina by remember { mutableStateOf("Servir caliente.") }
    var subCategoriaSeleccionada by remember { mutableStateOf(0) }

    val subCategorias = listOf("Galletas 🍪", "Cafés ☕", "Bebidas Frías 🥤", "Tortas 🍰")

    // Catálogo inicial de productos
    val productosMenu = remember {
        listOf(
            ProductoCatalogo("Galleta Chispas Clásica", 4.50, "Galletas 🍪", stock = 15),
            ProductoCatalogo("Galleta Red Velvet", 5.00, "Galletas 🍪", stock = 8),
            ProductoCatalogo("Café Espresso", 5.50, "Cafés ☕", stock = 50),
            ProductoCatalogo("Café Cappuccino", 7.50, "Cafés ☕", stock = 30),
            ProductoCatalogo("Cheesecake de Fresa", 8.50, "Tortas 🍰", stock = 4)
        )
    }

    // MAPA DE ESTADO ACTIVO PARA EL CARRITO (Une la interfaz con las acciones de agregar/quitar)
    val carritoState = remember { mutableStateMapOf<String, Int>() }

    // Filtrado e indicadores de la pestaña
    val productosFiltrados = productosMenu.filter { it.categoria == subCategorias[subCategoriaSeleccionada] }
    val cantidadItemsEnCarrito = carritoState.values.sum()
    val titulosPestanas = listOf("Menú de Productos 📋", "Ver Orden ($cantidadItemsEnCarrito) 🛒")

    // Calcular el total acumulado en base a lo que realmente hay en el carrito
    val totalAcumulado = productosMenu.sumOf { producto ->
        val cantidad = carritoState[producto.nombre] ?: 0
        cantidad * producto.precio // Al ser Double, Kotlin detecta automáticamente sumOf para Doubles
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Pedido", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    // BOTÓN VOLVER AL SALÓN EN LA BARRA SUPERIOR
                    IconButton(onClick = onBackToSalon) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al Salón",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            if (pestanaActiva == 1 && cantidadItemsEnCarrito > 0) {
                FloatingActionButton(
                    onClick = { mostrarConfirmarEnvio = true },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Confirmar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Pedido", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            // Tab de navegación interna
            TabRow(
                selectedTabIndex = pestanaActiva,
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White
            ) {
                titulosPestanas.forEachIndexed { index, titulo ->
                    Tab(
                        selected = pestanaActiva == index,
                        onClick = { pestanaActiva = index },
                        text = { Text(text = titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (pestanaActiva == 0) {
                // --- VISTA 1: MENÚ DE PRODUCTOS ---
                CabeceraInformacion(mozo = "Jherson 🧑‍🍳", mesa = "3 🪑", cliente = "Jonathan 👤", fecha = fechaHoraActual)

                ScrollableTabRow(
                    selectedTabIndex = subCategoriaSeleccionada,
                    containerColor = Color(0xFF2D3748),
                    contentColor = Color.White,
                    edgePadding = 16.dp
                ) {
                    subCategorias.forEachIndexed { index, cat ->
                        Tab(
                            selected = subCategoriaSeleccionada == index,
                            onClick = { subCategoriaSeleccionada = index },
                            text = { Text(text = cat, fontSize = 13.sp) }
                        )
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(productosFiltrados) { producto ->
                        val cantidadActual = carritoState[producto.nombre] ?: 0
                        FilaProductoCatalogo(
                            producto = producto,
                            cantidad = cantidadActual,
                            onAumentar = {
                                if (cantidadActual < producto.stock) {
                                    carritoState[producto.nombre] = cantidadActual + 1
                                }
                            },
                            onRestar = {
                                if (cantidadActual > 0) {
                                    val nuevaCantidad = cantidadActual - 1
                                    if (nuevaCantidad == 0) {
                                        carritoState.remove(producto.nombre)
                                    } else {
                                        carritoState[producto.nombre] = nuevaCantidad
                                    }
                                }
                            }
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                    }
                }
            } else {
                // --- VISTA 2: VER ORDEN (EL CARRITO) ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2F7)),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Detalle de la Orden Activa 📋",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E233D),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Mostrar solo los productos agregados
                                productosMenu.forEach { producto ->
                                    val cantidad = carritoState[producto.nombre] ?: 0
                                    if (cantidad > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "$cantidad x ${producto.nombre}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF334155)
                                            )
                                            Text(
                                                text = String.format("S/. %.2f", cantidad * producto.precio),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E233D)
                                            )
                                        }
                                    }
                                }

                                if (cantidadItemsEnCarrito == 0) {
                                    Text(
                                        text = "El carrito está vacío. Agrega productos desde el menú.",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = notasCocina,
                                    onValueChange = { notasCocina = it },
                                    label = { Text("Notas / Especificaciones de cocina") },
                                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Método de Pago Sugerido:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E233D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    metodosPago.forEach { metodo ->
                                        val seleccionado = metodoPagoSeleccionado == metodo
                                        FilterChip(
                                            selected = seleccionado,
                                            onClick = { metodoPagoSeleccionado = metodo },
                                            label = { Text(metodo, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF1E233D),
                                                selectedLabelColor = Color.White,
                                                containerColor = Color.White,
                                                labelColor = Color(0xFF334155)
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                selected = seleccionado,
                                                enabled = true,
                                                borderColor = Color.LightGray,
                                                selectedBorderColor = Color(0xFF1E233D),
                                                borderWidth = 1.dp,
                                                selectedBorderWidth = 1.5.dp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "TOTAL ACUMULADO:", fontSize = 15.sp, fontWeight = FontWeight.Black)
                                    Text(text = String.format("S/. %.2f", totalAcumulado), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE ENVÍO A COCINA ---
    if (mostrarConfirmarEnvio) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarEnvio = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "¿Confirmar y enviar orden? ☕",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E233D)
                )
            },
            text = {
                Text(
                    text = "El pedido por un total de S/. ${String.format("%.2f", totalAcumulado)} se registrará en el sistema y se enviará la comanda al área de preparación.",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarEnvio = false
                        onBackToSalon() // Cambia el estado en el MainContainerScreen a 0 (Salón)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Enviar a Cocina", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmarEnvio = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Revisar más", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
fun CabeceraInformacion(mozo: String, mesa: String, cliente: String, fecha: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Mozo: $mozo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                Text(text = "Mesa: $mesa", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Cliente: $cliente", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Fecha/Hora Solicitud: $fecha", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FilaProductoCatalogo(
    producto: ProductoCatalogo,
    cantidad: Int,
    onAumentar: () -> Unit,
    onRestar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = producto.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = String.format("S/. %.2f", producto.precio), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))

                Text(
                    text = "Stock: ${producto.stock}",
                    fontSize = 12.sp,
                    color = if (producto.stock <= 5) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(
                onClick = onRestar,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF1F5F9)),
                enabled = cantidad > 0
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
            }
            Text(text = "$cantidad", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = onAumentar,
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E233D), contentColor = Color.White),
                enabled = cantidad < producto.stock
            ) {
                Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PedidoScreenPreview() {
    PedidoScreen()
}