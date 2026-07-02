package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ConsumoMesa(val producto: String, val cantidad: Int, val totalFila: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(
    onIrAPedidoEdicion: () -> Unit,
    onRegresarAlSalon: () -> Unit
) {
    val nombreCliente = "Jonathan 👤"
    val numeroMesa = "Mesa 3 🪑"
    val mozoAsignado = "Jherson 🧑‍🍳"
    val metodoPagoSugerido = "Yape/Plin 📱"

    val listaConsumo = remember {
        listOf(
            ConsumoMesa("Galleta Chocochips", 2, 9.00),
            ConsumoMesa("Galleta Avena", 1, 4.00)
        )
    }

    val totalConsumido = listaConsumo.sumOf { it.totalFila }

    // --- ESTADO PARA EL DIÁLOGO DE SEGURIDAD ---
    var mostrarConfirmarCancelacion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Mesa", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    // BOTÓN VOLVER AL SALÓN EN LA BARRA SUPERIOR
                    IconButton(onClick = onRegresarAlSalon) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar al Salón",
                            tint = Color.White
                        )
                    }
                },
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

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = Color(0xFF1E233D).copy(alpha = 0.08f),
                                    contentColor = Color(0xFF1E233D),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = metodoPagoSugerido,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.1f),
                                    contentColor = Color(0xFFEF4444),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(text = "Ocupada", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Cliente: $nombreCliente", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        Text(text = "Atendido por: $mozoAsignado", fontSize = 14.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(text = "Consumo Actual de la Mesa 🍪", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D), modifier = Modifier.padding(bottom = 8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onIrAPedidoEdicion() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Modificar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modificar Productos del Pedido", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { mostrarConfirmarCancelacion = true },
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

    if (mostrarConfirmarCancelacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarCancelacion = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "¿Cancelar toda la comanda? ⚠️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFEF4444)
                )
            },
            text = {
                Text(
                    text = "Esta acción eliminará todos los consumos registrados de la $numeroMesa y liberará la mesa. No se puede deshacer.",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmarCancelacion = false
                        onRegresarAlSalon()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sí, Cancelar Pedido", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmarCancelacion = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Regresar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}