package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.ReportesNegocioViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesNegocioScreen(viewModel: ReportesNegocioViewModel = viewModel()) {
    val totalIngresos by viewModel.totalIngresos.collectAsState()
    val totalPedidos by viewModel.totalPedidos.collectAsState()
    val topProductos by viewModel.topProductos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var fechaInicioText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }
    var fechaFinText by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())) }

    LaunchedEffect(fechaInicioText, fechaFinText) {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val start = format.parse(fechaInicioText)
            val end = Calendar.getInstance().apply {
                time = format.parse(fechaFinText) ?: Date()
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            if (start != null) {
                viewModel.cargarReporte(start.time, end.time)
            }
        } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes del Negocio 📊", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // SELECTOR DE FECHAS
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Filtrar por Rango de Fechas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E233D)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fechaInicioText,
                            onValueChange = { fechaInicioText = it },
                            label = { Text("Desde") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = fechaFinText,
                            onValueChange = { fechaFinText = it },
                            label = { Text("Hasta") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // INDICADORES FINANCIEROS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IndicadorCard(
                    modifier = Modifier.weight(1f),
                    label = "Total Ingresos",
                    value = "S/ ${String.format(LocalLocale.current.platformLocale, "%.2f", totalIngresos)}",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF10B981)
                )

                IndicadorCard(
                    modifier = Modifier.weight(1f),
                    label = "Pedidos",
                    value = "$totalPedidos",
                    icon = Icons.Default.DateRange,
                    color = Color(0xFF1E233D)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EXPORTACIÓN
            Button(
                onClick = { /* Acción para construir PDF */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Reporte a PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // GRÁFICO DE BARRAS
            Text(
                text = "Top Productos Más Vendidos",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1E233D),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (topProductos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay datos de ventas disponibles.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(topProductos) { producto ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = producto.nombre,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "${producto.cantidadVendida} und.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E233D)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(20.dp)
                                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(6.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = producto.porcentaje)
                                        .fillMaxHeight()
                                        .background(
                                            color = Color(0xFF3B82F6),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndicadorCard(
    modifier: Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportesNegocioScreenPreview() {
    ReportesNegocioScreen()
}
