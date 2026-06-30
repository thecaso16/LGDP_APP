package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Estructura básica para representar una Mesa
data class Mesa(val numero: Int, val estaOcupada: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonScreen() {
    // Lista simulada de las mesas de la cafetería (puedes cambiar los estados "true" o "false")
    val listaMesas = remember {
        listOf(
            Mesa(1, estaOcupada = false),
            Mesa(2, estaOcupada = true),
            Mesa(3, estaOcupada = false),
            Mesa(4, estaOcupada = false),
            Mesa(5, estaOcupada = true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Disponibilidad de Mesas ☕",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E233D) // Mismo color institucional Navy del Login
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Leyenda de estados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IndicadorEstado(color = Color(0xFF10B981), texto = "Libre") // Verde esmeralda
                IndicadorEstado(color = Color(0xFFEF4444), texto = "Ocupada") // Rojo coral
            }

            Text(
                text = "Seleccione una mesa para atender:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Cuadrícula interactiva de 2 columnas para mostrar las mesas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(listaMesas.size) { index ->
                    val mesa = listaMesas[index]
                    ItemMesa(mesa = mesa)
                }
            }
        }
    }
}

@Composable
fun ItemMesa(mesa: Mesa) {
    // Determina el color de fondo según el estado de la mesa
    val colorFondo = if (mesa.estaOcupada) Color(0xFFEF4444) else Color(0xFF10B981)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                /* Más adelante aquí abriremos la comanda / pedido de esta mesa */
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "MESA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = mesa.numero.toString().padStart(2, '0'),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = if (mesa.estaOcupada) "Ocupada" else "Disponible",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun IndicadorEstado(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun SalonScreenPreview() {
    SalonScreen()
}