package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Estructura para listar lo que la mesa ya consumió y está guardado en la base de datos
data class ConsumoMesa(val producto: String, val cantidad: Int, val totalFila: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen() {
    // Datos simulados de la mesa ocupada (siguiendo tu ejemplo secuencial)
    val nombreCliente = "Jonathan 👤"
    val numeroMesa = "Mesa 3 🪑"
    val tiempoTranscurrido = "35 minutos"
    val mozoAsignado = "Jherson 🧑‍🍳"

    val listaConsumo = remember {
        listOf(
            ConsumoMesa("Galleta Chocochips", 2, 9.00),
            ConsumoMesa("Galleta Avena", 1, 4.00)
        )
    }

    val totalConsumido = listaConsumo.sumOf { it.totalFila }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Mesa", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // --- PARTE SUPERIOR: INFORMACIÓN DE LA OCUPACIÓN ---
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = numeroMesa, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            // Etiqueta de estado en rojo coral porque está ocupada
                            Surface(
                                color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                contentColor = Color(0xFFEF4444),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "Ocupada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Cliente: $nombreCliente", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        Text(text = "Atendido por: $mozoAsignado", fontSize = 14.sp, color = Color.Gray)
                        Text(text = "Tiempo en mesa: $tiempoTranscurrido ⏱️", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- PARTE CENTRAL: DETALLE DE LO CONSUMIDO HASTA EL MOMENTO ---
                Text(text = "Consumo Actual de la Mesa 🍪", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D), modifier = Modifier.padding(bottom = 8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Tabla de consumo
                        listaConsumo.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${item.cantidad} x ${item.producto}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                                Text(text = String.format("S/. %.2f", item.totalFila), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Subtotal acumulado en la mesa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "SUBTOTAL ACTUAL:", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                            Text(text = String.format("S/. %.2f", totalConsumido), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                        }
                    }
                }
            }

            // --- PARTE INFERIOR: ACCIONES DEL MOZO (BOTONES DE FLUJO) ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp) // Espaciado ligeramente más compacto
            ) {
                /// Botón 1: AHORA MODIFICAR (Permite sumar, restar o eliminar de la comanda actual)
                Button(
                    onClick = { /* Carlos abrirá PedidoScreen pasándole los datos actuales para editarlos */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Modificar") // Puedes mantener el icono o cambiarlo
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modificar Productos del Pedido", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Botón 2: Solicitar cuenta
                OutlinedButton(
                    onClick = { /* Envía alerta a la caja para imprimir precuenta */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = "Cuenta")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pedir Cuenta / Pre-cuenta", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Botón 3: NUEVO - Cancelar Pedido (Color rojo de advertencia)
                TextButton(
                    onClick = { /* Más adelante aquí Carlos abrirá un diálogo de confirmación */ },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(
                        text = "❌ Cancelar Todo el Pedido",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleMesaScreenPreview() {
    DetalleMesaScreen()
}