package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(onLogout: () -> Unit) {
    // 0: Salón
    // 1: Pendientes
    // 2: Detalle Mesa (No visible en barra)
    // 3: Caja
    // 4: Historial
    var pantallaActual by remember { mutableStateOf(0) }

    var mesaSeleccionadaId by remember { mutableStateOf<Int?>(null) }
    var clienteNombreSeleccionado by remember { mutableStateOf<String?>(null) }
    var pedidoSeleccionadoId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E233D),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // PESTAÑA 1: SALÓN
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = null
                        pedidoSeleccionadoId = null
                        pantallaActual = 0 
                    },
                    icon = { Icon(Icons.Default.TableRestaurant, contentDescription = "Salón") },
                    label = { Text("Salón", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                // PESTAÑA 2: PENDIENTES (Ahora con icono de bolsa)
                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = null
                        pedidoSeleccionadoId = null
                        pantallaActual = 1 
                    },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Pendientes") },
                    label = { Text("Pendientes", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                // PESTAÑA 3: HISTORIAL
                NavigationBarItem(
                    selected = pantallaActual == 4,
                    onClick = { pantallaActual = 4 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                // PESTAÑA 4: CAJA
                NavigationBarItem(
                    selected = pantallaActual == 3,
                    onClick = { pantallaActual = 3 },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Caja") },
                    label = { Text("Caja", fontWeight = FontWeight.Bold) },
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
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (pantallaActual) {
                0 -> SalonScreen(
                    onIrAPedido = { id, nombre -> 
                        mesaSeleccionadaId = id
                        clienteNombreSeleccionado = nombre
                        // Si es mesa libre, vamos directo a elegir productos
                        pedidoSeleccionadoId = null
                        pantallaActual = 5 // Index 5 para PedidoScreen
                    },
                    onIrADetalleMesa = { id ->
                        mesaSeleccionadaId = id
                        pedidoSeleccionadoId = null
                        pantallaActual = 2
                    },
                    onIrAPedidoParaLlevar = { 
                        mesaSeleccionadaId = null
                        clienteNombreSeleccionado = "Para Llevar"
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
                    onRegresar = { pantallaActual = 0 }
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
                    onIrAHistorial = { pantallaActual = 4 }
                )
                3 -> CuadreCajaScreen(
                    onRegresar = { pantallaActual = 0 }
                )
                4 -> ReportesTrabajadoresScreen(
                    onIrACierreCaja = { pantallaActual = 3 }
                )
////                5 -> PedidoScreen(
////                    mesaId = mesaSeleccionadaId,
////                    clienteNombre = clienteNombreSeleccionado,
////                    onBackToSalon = { pantallaActual = 0 }
//                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    MainContainerScreen(onLogout = {})
}