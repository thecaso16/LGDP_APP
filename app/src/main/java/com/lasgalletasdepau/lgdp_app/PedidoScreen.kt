package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoEntity
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(
    mesaId: Int? = null,
    clienteNombre: String? = null,
    onBackToSalon: () -> Unit = {},
    viewModel: PedidoViewModel = viewModel()
) {
    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val notasGlobales by viewModel.notasGlobales.collectAsState()

    var pestanaActiva by remember { mutableStateOf(0) }
    var categoriaSeleccionadaId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categorias) {
        if (categoriaSeleccionadaId == null && categorias.isNotEmpty()) {
            categoriaSeleccionadaId = categorias.first().id
        }
    }

    LaunchedEffect(mesaId) {
        if (mesaId != null && carrito.isEmpty()) {
            viewModel.cargarPedidoParaEdicion(mesaId = mesaId)
        }
    }

    val fechaHoraActual = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date()) }
    val productosFiltrados = productos.filter { it.categoriaId == categoriaSeleccionadaId }
    val cantidadItemsEnCarrito = carrito.values.sumOf { it.cantidad }
    val totalAcumulado = carrito.values.sumOf { it.cantidad * it.precioUnitario }

    val titulosPestanas = listOf("Catálogo 📋", "Carrito ($cantidadItemsEnCarrito) 🛒")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.limpiarCarrito()
                        onBackToSalon()
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            if (pestanaActiva == 1 && cantidadItemsEnCarrito > 0) {
                FloatingActionButton(
                    onClick = { viewModel.guardarPedido(mesaId, clienteNombre) { onBackToSalon() } },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Orden", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            TabRow(selectedTabIndex = pestanaActiva, containerColor = Color(0xFF1E233D), contentColor = Color.White) {
                titulosPestanas.forEachIndexed { index, titulo ->
                    Tab(
                        selected = pestanaActiva == index,
                        onClick = { pestanaActiva = index },
                        text = { Text(text = titulo, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (pestanaActiva) {
                0 -> {
                    // CATÁLOGO
                    CabeceraInformacion(
                        mozo = usuarioLogueado?.nombres ?: "...",
                        mesa = mesaId?.toString() ?: "Llevar",
                        cliente = clienteNombre ?: "Cliente",
                        fecha = fechaHoraActual
                    )
                    if (categorias.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = categorias.indexOfFirst { it.id == categoriaSeleccionadaId }.coerceAtLeast(0),
                            containerColor = Color(0xFF2D3748),
                            contentColor = Color.White,
                            edgePadding = 16.dp
                        ) {
                            categorias.forEach { cat ->
                                Tab(
                                    selected = categoriaSeleccionadaId == cat.id,
                                    onClick = { categoriaSeleccionadaId = cat.id },
                                    text = { Text(cat.nombre ?: "", fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(productosFiltrados) { producto ->
                            FilaProductoCatalogo(
                                producto = producto,
                                cantidad = carrito[producto.productoId]?.cantidad ?: 0,
                                onAumentar = { viewModel.agregarProducto(producto) },
                                onRestar = { viewModel.quitarProducto(producto.productoId) }
                            )
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                        }
                    }
                }
                1 -> {
                    // CARRITO
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Resumen de Orden 📋", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    carrito.forEach { (_, det) ->
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("${det.cantidad}x ${det.nombreProducto}", fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            Text("S/ ${String.format("%.2f", det.cantidad * det.precioUnitario)}", fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (carrito.isEmpty()) Text("El carrito está vacío", color = Color.Gray)
                                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                    Text("Notas del Pedido 📝", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = notasGlobales,
                                        onValueChange = { viewModel.actualizarNotasGlobales(it) },
                                        placeholder = { Text("Ej. Sin cubiertos, cliente tiene prisa...") },
                                        modifier = Modifier.fillMaxWidth().height(90.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("TOTAL:", fontWeight = FontWeight.Black)
                                        Text("S/ ${String.format("%.2f", totalAcumulado)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }
                        // Espacio extra al final para que el FAB no tape el total
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
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
                Text(text = "Mozo: $mozo 🧑‍🍳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                Text(text = "Mesa: $mesa 🪑", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Cliente: $cliente 👤", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Iniciado: $fecha", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun FilaProductoCatalogo(
    producto: ProductoEntity,
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
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(60.dp)) {
                AsyncImage(
                    model = producto.imagen,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = producto.nombre ?: "", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = String.format("S/. %.2f", producto.precio), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Text(
                        text = "Stock: ${producto.stock}",
                        fontSize = 12.sp,
                        color = if (producto.stock <= 5) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
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
