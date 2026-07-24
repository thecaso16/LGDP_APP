package com.lasgalletasdepau.lgdp_app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CajaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    userRole: String?,
    onLogout: () -> Unit,
    viewModel: CajaViewModel = viewModel()
) {
    // 0: Salón
    // 1: Pendientes
    // 2: Detalle Mesa
    // 3: Caja
    // 4: Historial
    // 5: PedidoScreen
    var pantallaActual by remember { mutableStateOf(0) }

    val user by viewModel.usuarioLogueado.collectAsState()
    var mesaSeleccionadaId by remember { mutableStateOf<Int?>(null) }
    var clienteNombreSeleccionado by remember { mutableStateOf<String?>(null) }
    var pedidoSeleccionadoId by remember { mutableStateOf<String?>(null) }

    val roles = RolUsuario.fromStringList(userRole)
    val esCajero = roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                            Text(
                                text = if (user != null) "${user?.nombres} ${user?.apellidos}" else "Cerrando sesión...",
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
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = null
                        pedidoSeleccionadoId = null
                        pantallaActual = 0 
                    },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Salón") },
                    label = { Text("Salón") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = null
                        pedidoSeleccionadoId = null
                        pantallaActual = 1 
                    },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Pendientes") },
                    label = { Text("Pendientes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                NavigationBarItem(
                    selected = pantallaActual == 4,
                    onClick = { pantallaActual = 4 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                if (esCajero) {
                    NavigationBarItem(
                        selected = pantallaActual == 3,
                        onClick = { pantallaActual = 3 },
                        icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Caja") },
                        label = { Text("Caja") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E233D),
                            selectedTextColor = Color.White,
                            indicatorColor = Color.White,
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
                    onLogout = onLogout
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    MainContainerScreen(userRole = "Administrador", onLogout = {})
}