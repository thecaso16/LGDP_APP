package com.lasgalletasdepau.lgdp_app.ui.main

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import com.lasgalletasdepau.lgdp_app.ui.theme.NavyBrand
import com.lasgalletasdepau.lgdp_app.ui.mesas.DetalleMesaScreen
import com.lasgalletasdepau.lgdp_app.ui.mesas.SalonScreen
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CajaViewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CuadreCajaScreen
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoScreen
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoViewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidosPendientesScreen
import com.lasgalletasdepau.lgdp_app.ui.pedidos.ReportesTrabajadoresScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    userRole: String?,
    onLogout: () -> Unit,
    viewModel: CajaViewModel = viewModel(),
    pedidoViewModel: PedidoViewModel = viewModel()
) {
    // 0: Salón
    // 1: Pendientes
    // 2: Detalle Mesa
    // 3: Caja
    // 4: Historial
    // 5: PedidoScreen
    var pantallaActual by remember { mutableStateOf(0) }

    val user by viewModel.usuarioLogueado.collectAsState()
    val carrito by pedidoViewModel.carrito.collectAsState()
    
    var mesaSeleccionadaId by remember { mutableStateOf<Int?>(null) }
    var clienteNombreSeleccionado by remember { mutableStateOf<String?>(null) }
    var pedidoSeleccionadoId by remember { mutableStateOf<String?>(null) }

    var mostrarConfirmarSalida by remember { mutableStateOf(false) }
    var pantallaDestino by remember { mutableStateOf<Int?>(null) }

    val roles = RolUsuario.fromStringList(userRole)
    val esCajero = roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (pantallaActual == 5) {
                        Text("Registrar Pedido", fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                val nombreCompleto = listOfNotNull(user?.nombres, user?.apellidos)
                                    .joinToString(" ")
                                    .ifBlank { "Usuario" }
                                Text(
                                    text = if (user != null) nombreCompleto else "Cerrando sesión...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = user?.rol ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (pantallaActual == 5 || pantallaActual == 2) {
                        IconButton(onClick = {
                            if (pantallaActual == 5) {
                                if (carrito.isNotEmpty()) {
                                    pantallaDestino = 0
                                    mostrarConfirmarSalida = true
                                } else {
                                    pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                                    mesaSeleccionadaId = null
                                    clienteNombreSeleccionado = null
                                    pedidoSeleccionadoId = null
                                    pantallaActual = 0
                                }
                            } else {
                                pantallaActual = 0
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (pantallaActual != 5) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBrand)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else NavyBrand,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { 
                        if (pantallaActual == 5 && carrito.isNotEmpty()) {
                            pantallaDestino = 0
                            mostrarConfirmarSalida = true
                        } else {
                            if (pantallaActual == 5) pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                            mesaSeleccionadaId = null
                            clienteNombreSeleccionado = null
                            pedidoSeleccionadoId = null
                            pantallaActual = 0 
                        }
                    },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Salón") },
                    label = { Text("Salón") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { 
                        if (pantallaActual == 5 && carrito.isNotEmpty()) {
                            pantallaDestino = 1
                            mostrarConfirmarSalida = true
                        } else {
                            if (pantallaActual == 5) pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                            mesaSeleccionadaId = null
                            clienteNombreSeleccionado = null
                            pedidoSeleccionadoId = null
                            pantallaActual = 1 
                        }
                    },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Pendientes") },
                    label = { Text("Pendientes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = pantallaActual == 4,
                    onClick = { 
                        if (pantallaActual == 5 && carrito.isNotEmpty()) {
                            pantallaDestino = 4
                            mostrarConfirmarSalida = true
                        } else {
                            if (pantallaActual == 5) pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                            mesaSeleccionadaId = null
                            clienteNombreSeleccionado = null
                            pedidoSeleccionadoId = null
                            pantallaActual = 4 
                        }
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                if (esCajero) {
                    NavigationBarItem(
                        selected = pantallaActual == 3,
                        onClick = { 
                            if (pantallaActual == 5 && carrito.isNotEmpty()) {
                                pantallaDestino = 3
                                mostrarConfirmarSalida = true
                            } else {
                                if (pantallaActual == 5) pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                                mesaSeleccionadaId = null
                                clienteNombreSeleccionado = null
                                pedidoSeleccionadoId = null
                                pantallaActual = 3 
                            }
                        },
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Caja") },
                        label = { Text("Caja") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (pantallaActual) {
                0 -> SalonScreen(
                    onIrAPedido = { id, nombre -> 
                        mesaSeleccionadaId = id
                        clienteNombreSeleccionado = nombre
                        pedidoSeleccionadoId = null
                        pantallaActual = 5 
                    },
                    onIrADetalleMesa = { id ->
                        mesaSeleccionadaId = id
                        pedidoSeleccionadoId = null
                        pantallaActual = 2
                    },
                    onIrAPedidoParaLlevar = { nombre -> 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = nombre
                        pedidoSeleccionadoId = null
                        pantallaActual = 5 
                    },
                    onLogout = onLogout
                )
                1 -> PedidosPendientesScreen(
                    onVerDetalle = { pid ->
                        pedidoSeleccionadoId = pid
                        mesaSeleccionadaId = null
                        pantallaActual = 2
                    },
                    onRegresar = { pantallaActual = 0 },
                    onLogout = onLogout
                )
                2 -> DetalleMesaScreen(
                    mesaId = mesaSeleccionadaId,
                    pedidoId = pedidoSeleccionadoId,
                    onIrAPedidoEdicion = { mid, pid ->
                        mesaSeleccionadaId = mid
                        pedidoSeleccionadoId = pid
                        pantallaActual = 5
                    },
                    onRegresarAlSalon = { pantallaActual = 0 },
                    onIrAHistorial = { pantallaActual = 4 },
                    onLogout = onLogout
                )
                3 -> if (esCajero) {
                    CuadreCajaScreen(
                        onRegresar = { pantallaActual = 0 },
                        onLogout = onLogout
                    )
                } else {
                    pantallaActual = 0
                }
                4 -> ReportesTrabajadoresScreen(
                    onIrACierreCaja = { pantallaActual = 3 },
                    onLogout = onLogout
                )
                5 -> PedidoScreen(
                    mesaId = mesaSeleccionadaId,
                    pedidoId = pedidoSeleccionadoId,
                    clienteNombre = clienteNombreSeleccionado,
                    onBackToSalon = { pantallaActual = 0 },
                    onLogout = onLogout,
                    viewModel = pedidoViewModel
                )
            }
        }
    }

    if (mostrarConfirmarSalida) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarSalida = false },
            title = { Text("¿Descartar pedido?") },
            text = { Text("Hay productos en el carrito. Si sales, se perderán los cambios y se liberará la mesa si es nueva.") },
            confirmButton = {
                Button(
                    onClick = {
                        pedidoViewModel.cancelarPedido(mesaSeleccionadaId)
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = null
                        pedidoSeleccionadoId = null
                        pantallaDestino?.let { pantallaActual = it }
                        mostrarConfirmarSalida = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmarSalida = false }) { 
                    Text("Continuar pidiendo", color = if(isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.primary) 
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    MainContainerScreen(userRole = "Administrador", onLogout = {})
}
