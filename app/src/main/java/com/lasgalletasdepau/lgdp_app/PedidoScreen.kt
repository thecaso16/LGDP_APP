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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoEntity
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoScreen(
    mesaId: Int? = null,
    pedidoId: String? = null,
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

    LaunchedEffect(mesaId, pedidoId) {
        if (carrito.isEmpty()) {
            if (pedidoId != null || mesaId != null) {
                viewModel.cargarPedidoParaEdicion(mesaId = mesaId, pedidoId = pedidoId)
            }
        }
    }

    val fechaHoraActual = remember { SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date()) }
    val productosFiltrados = productos.filter { it.categoriaId == categoriaSeleccionadaId }
    val cantidadItemsEnCarrito = carrito.values.sumOf { it.cantidad }
    val totalAcumulado = carrito.values.sumOf { it.cantidad * it.precioUnitario }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tomar Pedido 📋", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.limpiarCarrito()
                        onBackToSalon()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        floatingActionButton = {
            if (pestanaActiva == 1 && cantidadItemsEnCarrito > 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.guardarPedido(mesaId, clienteNombre) { onBackToSalon() } },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirmar Orden", fontWeight = FontWeight.Bold)
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
            // TABS
            TabRow(selectedTabIndex = pestanaActiva, containerColor = Color(0xFF1E233D), contentColor = Color.White) {
                Tab(selected = pestanaActiva == 0, onClick = { pestanaActiva = 0 }, text = { Text("Menú 📋") })
                Tab(selected = pestanaActiva == 1, onClick = { pestanaActiva = 1 }, text = { Text("Carrito ($cantidadItemsEnCarrito) 🛒") })
            }

            if (categorias.isEmpty()) {
                // Estado de carga o vacío
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF1E233D))
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando Menú...", color = Color.Gray)
                    }
                }
            } else {
                when (pestanaActiva) {
                    0 -> {
                        // VISTA CATÁLOGO
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

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                CabeceraInformacion(
                                    mozo = usuarioLogueado?.nombres ?: "...",
                                    mesa = mesaId?.toString() ?: "Llevar",
                                    cliente = clienteNombre ?: "Cliente",
                                    fecha = fechaHoraActual
                                )
                            }
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
                        // VISTA CARRITO
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Resumen de Orden 🍪", fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(16.dp))
                                        
                                        carrito.forEach { (_, det) ->
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("${det.cantidad}x ${det.nombreProducto}", modifier = Modifier.weight(1f))
                                                Text("S/ ${String.format("%.2f", det.cantidad * det.precioUnitario)}", fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                        }

                                        if (carrito.isEmpty()) {
                                            Text("El carrito está vacío.", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                        }

                                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                                        
                                        Text("Notas del Pedido 📝", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        OutlinedTextField(
                                            value = notasGlobales,
                                            onValueChange = { viewModel.actualizarNotasGlobales(it) },
                                            placeholder = { Text("Ej. Sin azúcar, para las 5pm...") },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(Modifier.height(16.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("TOTAL:", fontWeight = FontWeight.Black, fontSize = 18.sp)
                                            Text("S/ ${String.format("%.2f", totalAcumulado)}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF10B981))
                                        }
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(80.dp)) }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mozo: $mozo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Mesa: $mesa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text("Cliente: $cliente", fontSize = 13.sp, color = Color.DarkGray)
            Text("Fecha: $fecha", fontSize = 11.sp, color = Color.Gray)
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(producto.nombre ?: "", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (!producto.descripcion.isNullOrBlank()) {
                Text(
                    producto.descripcion,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "S/ ${String.format("%.2f", producto.precio)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E233D),
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(12.dp))
                Surface(
                    color = if (producto.stock > 0) Color(0xFFE2E8F0) else Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "Stock: ${producto.stock}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (producto.stock > 0) Color(0xFF475569) else Color.Red
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRestar,
                enabled = cantidad > 0,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (cantidad > 0) Color.Red else Color.LightGray
                )
            }
            Text(
                "$cantidad",
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 16.sp
            )
            IconButton(
                onClick = onAumentar,
                enabled = cantidad < producto.stock,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = if (cantidad < producto.stock) Color(0xFF10B981) else Color.LightGray
                )
            }
        }
    }
}
