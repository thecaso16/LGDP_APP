package com.tuapp.restaurante.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lasgalletasdepau.lgdp_app.DetalleMesaScreen

// 1. Modelo de datos para representar una comanda en el historial
data class ComandaHistorial(
    val id: String,
    val mesa: String,
    val mozo: String,
    val total: Double,
    val hora: String,
    val estado: EstadoComanda,
    val items: List<String>
)

enum class EstadoComanda {
    EN_COCINA, ENTREGADA, COBRADA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistorialScreen(modifier: Modifier = Modifier) {
    // Estado para saber qué pestaña está seleccionada (0 = Todas, 1 = En Cocina, 2 = Cobradas)
    var tabSeleccionada by remember { mutableStateOf(0) }
    val titulosTabs = listOf("Todas", "En Cocina", "Cobradas")

    // Datos simulados de ejemplo
    val listaComandas = remember {
        listOf(
            ComandaHistorial("001", "Mesa 4", "Carlos R.", 45.50, "12:30 PM", EstadoComanda.EN_COCINA, listOf("1x Ceviche Clásico", "1x Chicha Morada")),
            ComandaHistorial("002", "Mesa 1", "Ana M.", 82.00, "11:45 AM", EstadoComanda.COBRADA, listOf("2x Lomo Saltado", "2x Gaseosa")),
            ComandaHistorial("003", "Mesa 7", "Carlos R.", 35.00, "01:15 PM", EstadoComanda.EN_COCINA, listOf("1x Arroz con Pollo")),
            ComandaHistorial("004", "Mesa 2", "Ana M.", 120.00, "10:30 AM", EstadoComanda.COBRADA, listOf("1x Jalea Personal", "1x Parihuela", "1x Jarra de Chicha"))
        )
    }

    // Filtramos la lista según la pestaña seleccionada
    val comandasFiltradas = when (tabSeleccionada) {
        1 -> listaComandas.filter { it.estado == EstadoComanda.EN_COCINA }
        2 -> listaComandas.filter { it.estado == EstadoComanda.COBRADA }
        else -> listaComandas // "Todas"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Comandas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de pestañas para filtrar rápidamente
            TabRow(selectedTabIndex = tabSeleccionada) {
                titulosTabs.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = tabSeleccionada == indice,
                        onClick = { tabSeleccionada = indice },
                        text = { Text(titulo, fontSize = 14.sp) }
                    )
                }
            }

            // Lista vertical con las comandas filtradas
            if (comandasFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay comandas en esta categoría", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(comandasFiltradas) { comanda ->
                        TarjetaComandaHistorial(comanda = comanda)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaComandaHistorial(comanda: ComandaHistorial) {
    // Definimos el color del tag según el estado de la comanda
    val colorEstado = when (comanda.estado) {
        EstadoComanda.EN_COCINA -> Color(0xFFFF9800) // Naranja
        EstadoComanda.ENTREGADA -> Color(0xFF2196F3) // Azul
        EstadoComanda.COBRADA -> Color(0xFF4CAF50)   // Verde
    }

    val textoEstado = when (comanda.estado) {
        EstadoComanda.EN_COCINA -> "En Cocina"
        EstadoComanda.ENTREGADA -> "Entregada"
        EstadoComanda.COBRADA -> "Cobrada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Mesa y Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comanda.mesa,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                // Tag de Estado
                Surface(
                    color = colorEstado.copy(alpha = 0.15f),
                    contentColor = colorEstado,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = textoEstado,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Info secundaria: Mozo y Hora
            Text(
                text = "Mozo: ${comanda.mozo} • ${comanda.hora}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Lista de platillos pedidos
            Text(
                text = "Detalle del pedido:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            comanda.items.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Fila inferior: Total de la comanda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("S/ %.2f", comanda.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHistorialScreenPreview() {AdminHistorialScreen()}