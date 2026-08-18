package com.lasgalletasdepau.lgdp_app.ui.pedidos

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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
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
    onLogout: () -> Unit,
    viewModel: PedidoViewModel = viewModel()
) {
    val usuarioLogueado by viewModel.usuarioLogueado.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val carrito by viewModel.carrito.collectAsState()
    val notasGlobales by viewModel.notasGlobales.collectAsState()

    var pestanaActiva by remember { mutableStateOf(0) }
    var categoriaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    LaunchedEffect(categorias) {
        if (categoriaSeleccionadaId == null && categorias.isNotEmpty()) {
            // Buscamos "Sánguches" con o sin tilde para mayor seguridad
            val sanguchCat = categorias.find { 
                it.nombre?.contains("Sánguches", ignoreCase = true) == true || 
                it.nombre?.contains("Sanguches", ignoreCase = true) == true 
            }
            categoriaSeleccionadaId = sanguchCat?.id ?: categorias.first().id
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = pestanaActiva,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pestanaActiva]),
                        color = Color.White
                    )
                }
            ) {
                Tab(selected = pestanaActiva == 0, onClick = { pestanaActiva = 0 }, text = { Text("Menú") })
                Tab(selected = pestanaActiva == 1, onClick = { pestanaActiva = 1 }, text = { Text("Carrito ($cantidadItemsEnCarrito)") })
            }

            if (categorias.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Cargando carta...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                when (pestanaActiva) {
                    0 -> {
                        ScrollableTabRow(
                            selectedTabIndex = categorias.indexOfFirst { it.id == categoriaSeleccionadaId }.coerceAtLeast(0),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            edgePadding = 16.dp
                        ) {
                            categorias.forEach { cat ->
                                Tab(
                                    selected = categoriaSeleccionadaId == cat.id,
                                    onClick = { categoriaSeleccionadaId = cat.id },
                                    text = { Text(cat.nombre ?: "", style = MaterialTheme.typography.labelLarge) }
                                )
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                CabeceraInformacion(
                                    mozo = usuarioLogueado?.nombres ?: "...",
                                    mesa = mesaId?.toString() ?: "Para llevar",
                                    cliente = clienteNombre ?: "Cliente general",
                                    fecha = fechaHoraActual
                                )
                            }
                            items(productosFiltrados) { producto ->
                                FilaProductoCatalogo(
                                    producto = producto,
                                    cantidad = carrito[producto.id]?.cantidad ?: 0,
                                    onAumentar = { viewModel.agregarProducto(producto) },
                                    onRestar = { viewModel.quitarProducto(producto.id) }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            }
                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Resumen de Orden", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(16.dp))

                                        carrito.forEach { (_, det) ->
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("${det.cantidad}x ${det.nombreProducto}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                                Text("S/ ${String.format("%.2f", det.cantidad * det.precioUnitario)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Spacer(Modifier.height(8.dp))
                                        }

                                        if (carrito.isEmpty()) {
                                            Text("El carrito está vacío.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
                                        }

                                        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                                        Text("Notas del pedido", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = notasGlobales,
                                            onValueChange = { viewModel.actualizarNotasGlobales(it) },
                                            placeholder = { Text("Ej. Sin azúcar, servido a las 5:00 PM...") },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(Modifier.height(20.dp))
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                            Text("TOTAL:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                            Text("S/ ${String.format("%.2f", totalAcumulado)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }

        // Floating Action Button manual
        if (pestanaActiva == 1 && cantidadItemsEnCarrito > 0) {
            ExtendedFloatingActionButton(
                onClick = { viewModel.guardarPedido(mesaId, clienteNombre) { onBackToSalon() } },
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Confirmar Pedido", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CabeceraInformacion(mozo: String, mesa: String, cliente: String, fecha: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Atendido por: $mozo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Mesa: $mesa", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Text("Cliente: $cliente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Fecha y hora: $fecha", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun FilaProductoCatalogo(
    producto: Producto,
    cantidad: Int,
    onAumentar: () -> Unit,
    onRestar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    producto.nombre, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!producto.descripcion.isNullOrBlank()) {
                    Text(
                        producto.descripcion,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "S/ ${String.format("%.2f", producto.precio)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        color = if (producto.stock > 0) MaterialTheme.colorScheme.surfaceVariant else Color.Red.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Stock: ${producto.stock}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (producto.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else Color.Red
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRestar,
                    enabled = cantidad > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = if (cantidad > 0) Color.Red else MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Text(
                    "$cantidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = onAumentar,
                    enabled = cantidad < producto.stock,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = if (cantidad < producto.stock) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

