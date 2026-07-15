package com.lasgalletasdepau.lgdp_app

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
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CajaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContainerScreen(
    onLogout: () -> Unit,
    viewModel: CajaViewModel = viewModel()
) {
    // 0: Gestión de Personal
    // 1: Catálogo de Productos
    // 2: Inventario de Insumos (Oculto)
    // 3: Reportes del Negocio
    // 4: Desempeño de Equipo
    var pantallaActual by remember { mutableStateOf(0) }
    val user by viewModel.usuarioLogueado.collectAsState()

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
                /* NavigationBarItem(
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
                ) */
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (pantallaActual) {
                0 -> GestionUsuariosScreen(onLogout = onLogout)
                1 -> GestionCatalogoScreen(onLogout = onLogout)
                // 2 -> GestionInventarioScreen(onLogout = onLogout)
                3 -> ReportesNegocioScreen(onLogout = onLogout)
                4 -> DesempenoPedidosScreen(onLogout = onLogout)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminContainerScreenPreview() {
    AdminContainerScreen(onLogout = {})
}