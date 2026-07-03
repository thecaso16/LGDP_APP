package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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

@Composable
fun MainContainerScreen() {
    // CONTROL DE NAVEGACIÓN ACTUALIZADO
    // 0: Salón (SalonScreen)
    // 1: Pedido (PedidoScreen)
    // 2: Detalle de Mesa (DetalleMesaScreen)
    // 3: Cierre de Caja (CuadreCajaScreen)
    // 4: Historial de Pedidos Trabajador (ReportesTrabajadoresScreen) -> ¡Nueva Pantalla!
    var pantallaActual by remember { mutableStateOf(0) }

    var pantallaMenuPrevia by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            // Reemplazamos la barra de un solo botón por una barra de navegación completa tipo Airbnb/WhatsApp
            NavigationBar(
                containerColor = Color(0xFF1E233D), // Navy institucional de Las Galletas de Pau
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // PESTAÑA 1: SALÓN
                NavigationBarItem(
                    selected = pantallaActual == 0 || pantallaActual == 2, // Se marca si está en salón o viendo el detalle de una mesa
                    onClick = { pantallaActual = 0 },
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

                // PESTAÑA 2: NUEVO PEDIDO / PARA LLEVAR
                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { pantallaActual = 1 },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Pedido") },
                    label = { Text("Pedido", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                // PESTAÑA 3: HISTORIAL (La que faltaba para el trabajador)
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
                    onIrAPedido = { pantallaActual = 1 },
                    onIrADetalleMesa = {
                        pantallaMenuPrevia = 0
                        pantallaActual = 2
                    },
                    onIrACierreCaja = { pantallaActual = 3 },
                    onIrAPedidoParaLlevar = { pantallaActual = 1 }
                )
                1 -> PedidoScreen(
                    onBackToSalon = { pantallaActual = 0 }
                )
                2 -> DetalleMesaScreen(
                    onIrAPedidoEdicion = { pantallaActual = 1 },
                    onRegresarAlSalon = { pantallaActual = pantallaMenuPrevia }
                )
                3 -> CuadreCajaScreen(
                    onRegresar = { pantallaActual = 0 }
                )
                4 -> ReportesTrabajadoresScreen() // 👈 Integrada la pantalla de historial de pedidos para el mozo
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    MainContainerScreen()
}