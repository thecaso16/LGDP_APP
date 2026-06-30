package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
data class ProductoCatalogo(val nombre: String, val precio: Double, val categoria: String)
data class ElementoCarrito(val producto: String, val cantidad: Int, val precioUnitario: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen() {
    // 1. CONTROL DE LA PESTAÑA PRINCIPAL (0: Menú de Productos, 1: Ver Orden)
    var pestanaActiva by remember { mutableStateOf(0) }
    val titulosPestanas = listOf("Menú de Productos 📋", "Ver Orden (2) 🛒")

    // Datos automáticos de tu propuesta
    val fechaHoraActual = remember {
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
    }
    var notasCocina by remember { mutableStateOf("Servir caliente.") }
    var subCategoriaSeleccionada by remember { mutableStateOf(0) }

    val subCategorias = listOf("Galletas 🍪", "Cafés ☕", "Bebidas Frías 🥤", "Tortas 🍰")

    // Productos simulados en el catálogo
    val productosMenu = remember {
        listOf(
            ProductoCatalogo("Galleta Chispas Clásica", 4.50, "Galletas 🍪"),
            ProductoCatalogo("Galleta Red Velvet", 5.00, "Galletas 🍪"),
            ProductoCatalogo("Café Espresso", 5.50, "Cafés ☕"),
            ProductoCatalogo("Café Cappuccino", 7.50, "Cafés ☕"),
            ProductoCatalogo("Cheesecake de Fresa", 8.50, "Tortas 🍰")
        )
    }

    // Productos que ya están agregados al carrito (Tu ejemplo)
    val productosEnCarrito = remember {
        listOf(
            ElementoCarrito("Galleta Chispas Clásica", 2, 4.50),
            ElementoCarrito("Galleta Red Velvet", 1, 5.00)
        )
    }

    val productosFiltrados = productosMenu.filter { it.categoria == subCategorias[subCategoriaSeleccionada] }
    val totalAcumulado = productosEnCarrito.sumOf { it.cantidad * it.precioUnitario }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Pedido", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            // El botón de confirmar solo se muestra si estamos revisando la orden
            if (pestanaActiva == 1) {
                FloatingActionButton(
                    onClick = { /* Lógica para guardar el pedido */ },
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
            // 2. BARRA DE PESTAÑAS PRINCIPALES (Menú vs Ver Orden)
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

            // 3. CONTROL DE FLUJO: Dependiendo de la pestaña activa, mostramos una vista u otra
            if (pestanaActiva == 0) {
                // --- VISTA 1: MENÚ DE PRODUCTOS ---
                // Cabecera fija con datos de atención
                CabeceraInformacion(mozo = "Jherson 🧑‍🍳", mesa = "3 🪑", cliente = "Jonathan 👤", fecha = fechaHoraActual)

                // Subcategorías deslizables
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

                // Lista de productos del catálogo
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(productosFiltrados) { producto ->
                        FilaProductoCatalogo(producto = producto)
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

                                // Lista los productos añadidos en tu ejemplo
                                productosEnCarrito.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.cantidad} x ${item.producto}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF334155)
                                        )
                                        Text(
                                            text = String.format("S/. %.2f", item.cantidad * item.precioUnitario),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E233D)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                // Tu campo de Notas sugerido
                                OutlinedTextField(
                                    value = notasCocina,
                                    onValueChange = { notasCocina = it },
                                    label = { Text("Notas / Especificaciones de cocina") },
                                    modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Total Acumulado
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
fun FilaProductoCatalogo(producto: ProductoCatalogo) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = producto.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(text = String.format("S/. %.2f", producto.precio), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = {}, modifier = Modifier.size(32.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFF1F5F9))) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
            }
            Text(text = "0", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}, modifier = Modifier.size(32.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E233D), contentColor = Color.White)) {
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