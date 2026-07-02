package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Control (Admin)", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECCIÓN 1: METRICAS CLAVE ---
            item {
                Text(text = "Resumen del Día 📊", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaMetrica(
                            titulo = "Ingresos",
                            valor = "S/. 428.50",
                            icono = Icons.Default.AttachMoney,
                            colorIcono = Color(0xFF10B981)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        TarjetaMetrica(
                            titulo = "Pedidos",
                            valor = "18 Órdenes",
                            icono = Icons.Default.LocalMall,
                            colorIcono = Color(0xFF3B82F6)
                        )
                    }
                }
            }

            // --- SECCIÓN 2: ALERTA DE STOCK CRÍTICO ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)), // Fondo rojizo suave de advertencia
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alertas de Stock Crítico (<= 5) 🚨",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Lista de productos bajos en stock
                        FilaStockCritico(producto = "Cheesecake de Fresa", stockActual = 4)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFFCA5A5).copy(alpha = 0.3f))
                        FilaStockCritico(producto = "Galleta Red Velvet", stockActual = 5)
                    }
                }
            }

            // --- SECCIÓN 3: RENDIMIENTO POR MÉTODOS DE PAGO ---
            item {
                Text(text = "Ventas por Método de Pago 💳", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilaMetodoPago(metodo = "Yape / Plin 📱", monto = "S/. 250.00", porcentaje = 0.58f, colorBarra = Color(0xFF8B5CF6))
                        FilaMetodoPago(metodo = "Efectivo 💵", monto = "S/. 128.50", porcentaje = 0.30f, colorBarra = Color(0xFF10B981))
                        FilaMetodoPago(metodo = "Izipay (Tarjetas) 💳", monto = "S/. 50.00", porcentaje = 0.12f, colorBarra = Color(0xFF3B82F6))
                    }
                }
            }

            // --- SECCIÓN 4: GRÁFICO DE BARRAS VERTICALES ---
            item {
                Text(text = "Flujo de Ventas Estimado por Hora ⏱️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Barras simuladas (Altura proporcional al flujo de ventas en esa hora)
                            BarraGrafico(alturaPorcentaje = 0.25f, hora = "4pm")
                            BarraGrafico(alturaPorcentaje = 0.50f, hora = "5pm")
                            BarraGrafico(alturaPorcentaje = 0.95f, hora = "6pm") // Hora pico
                            BarraGrafico(alturaPorcentaje = 0.75f, hora = "7pm")
                            BarraGrafico(alturaPorcentaje = 0.40f, hora = "8pm")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaMetrica(titulo: String, valor: String, icono: ImageVector, colorIcono: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = titulo, tint = colorIcono, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = titulo, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(text = valor, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
        }
    }
}

@Composable
fun FilaStockCritico(producto: String, stockActual: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = producto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF7F1D1D))
        Surface(
            color = Color(0xFFEF4444),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "$stockActual unid.",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun FilaMetodoPago(metodo: String, monto: String, porcentaje: Float, colorBarra: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = metodo, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
            Text(text = monto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Barra de progreso que ilustra el porcentaje del total
        LinearProgressIndicator(
            progress = { porcentaje },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = colorBarra,
            trackColor = Color(0xFFF1F5F9),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun RowScope.BarraGrafico(alturaPorcentaje: Float, hora: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        // La barra en sí
        Box(
            modifier = Modifier
                .fillMaxHeight(alturaPorcentaje)
                .width(16.dp)
                .background(Color(0xFF1E233D), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = hora, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    AdminDashboardScreen()
}