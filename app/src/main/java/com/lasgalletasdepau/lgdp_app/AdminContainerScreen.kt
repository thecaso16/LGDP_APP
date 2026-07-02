package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.tuapp.restaurante.ui.screens.AdminHistorialScreen

@Composable
fun AdminContainerScreen() {
    // 0: Dashboard (Panel Principal)
    // 1: Inventario / Productos
    // 2: Historial de Ventas
    var pantallaActual by remember { mutableStateOf(2) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E233D), // Mantenemos el Navy institucional
                contentColor = Color.White
            ) {
                // Opción 1: Panel de Control (Dashboard)
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { pantallaActual = 0 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Dashboard") },
                    label = { Text("Panel", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )

                // Opción 2: Gestión de Productos
                NavigationBarItem(
                    selected = pantallaActual == 1,
                    onClick = { pantallaActual = 1 },
                    icon = { Icon(Icons.Default.Cookie, contentDescription = "Productos") },
                    label = { Text("Productos", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
                    )
                )

                // Opción 3: Historial de Comandas
                NavigationBarItem(
                    selected = pantallaActual == 2,
                    onClick = { pantallaActual = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.LightGray,
                        unselectedTextColor = Color.LightGray
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
                0 -> AdminDashboardScreen()
                1 -> AdminProductosScreen()
                2 -> AdminHistorialScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminContainerScreenPreview() {
    AdminContainerScreen()
}