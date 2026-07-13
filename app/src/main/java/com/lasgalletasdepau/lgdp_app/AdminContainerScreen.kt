package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.RestaurantMenu
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
fun AdminContainerScreen(onLogout: () -> Unit) {
    // CONTROL DE NAVEGACIÓN INTERNA DEL ADMIN
    // 0: Gestión de Personal (GestionUsuariosScreen)
    // 1: Catálogo de Productos (GestionCatalogoScreen)
    // 2: Inventario de Insumos (GestionInventarioScreen)
    // 3: Reportes del Negocio (ReportesNegocioScreen)
    // 4: Reportes de Trabajadores (ReportesTrabajadoresScreen)
    var pantallaActual by remember { mutableStateOf(2) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E233D), // Nuestro Navy institucional
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = pantallaActual == 0,
                    onClick = { pantallaActual = 0 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Personal") },
                    label = { Text("Personal") },
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
                    onClick = { pantallaActual = 1 },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Catálogo") },
                    label = { Text("Catálogo") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = pantallaActual == 2,
                    onClick = { pantallaActual = 2 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Insumos") },
                    label = { Text("Insumos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1E233D),
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                    )
                )
                NavigationBarItem(
                    selected = pantallaActual == 3,
                    onClick = { pantallaActual = 3 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Negocio") },
                    label = { Text("Negocio") },
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
                    icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Desempeño") },
                    label = { Text("Desempeño") },
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
                0 -> GestionUsuariosScreen(onLogout = onLogout)
                1 -> GestionCatalogoScreen(onLogout = onLogout)
                2 -> GestionInventarioScreen(onLogout = onLogout)
                3 -> ReportesNegocioScreen(onLogout = onLogout)
                4 -> DesempenoPedidosScreen(onLogout = onLogout) // Usaremos la nueva pantalla
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminContainerScreenPreview() {
    AdminContainerScreen(onLogout = {})
}