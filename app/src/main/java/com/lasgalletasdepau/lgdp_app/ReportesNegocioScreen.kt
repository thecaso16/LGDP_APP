package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.ReportesNegocioViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesNegocioScreen(
    viewModel: ReportesNegocioViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val totalIngresos by viewModel.totalIngresos.collectAsState()
    val totalPedidos by viewModel.totalPedidos.collectAsState()
    val topProductos by viewModel.topProductos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    // Función para corregir el desfase UTC de DatePicker y obtener el inicio/fin real
    fun getCorrectedMillis(utcMillis: Long?): Long {
        if (utcMillis == null) return System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = utcMillis
        val localCalendar = Calendar.getInstance()
        localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        return localCalendar.timeInMillis
    }

    LaunchedEffect(datePickerStateStart.selectedDateMillis, datePickerStateEnd.selectedDateMillis) {
        val start = getCorrectedMillis(datePickerStateStart.selectedDateMillis)
        val end = getCorrectedMillis(datePickerStateEnd.selectedDateMillis)
        viewModel.cargarReporte(start, end)
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Aceptar") } }
        ) { DatePicker(state = datePickerStateStart) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Aceptar") } }
        ) { DatePicker(state = datePickerStateEnd) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes del Negocio 📊", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { /* Implementar Exportar Excel luego */ }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Exportar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
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
        ) {
            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filtrar por Rango de Fechas", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis))),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Desde") },
                            trailingIcon = { IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Default.CalendarMonth, null) } },
                            modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }
                        )

                        OutlinedTextField(
                            value = sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis))),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hasta") },
                            trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.CalendarMonth, null) } },
                            modifier = Modifier.weight(1f).clickable { showEndDatePicker = true }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IndicadorCard(Modifier.weight(1f), "Ingresos", "S/ ${String.format(locale, "%.2f", totalIngresos)}", Icons.Default.TrendingUp, Color(0xFF10B981))
                IndicadorCard(Modifier.weight(1f), "Pedidos", "$totalPedidos", Icons.Default.DateRange, Color(0xFF1E233D))
            }

            Text("Top Productos Vendidos", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))

            if (topProductos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay datos en este rango.", color = Color.Gray) }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(topProductos) { prod ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(prod.nombre, fontSize = 13.sp)
                                Text("${prod.cantidadVendida} und.", fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))) {
                                Box(modifier = Modifier.fillMaxWidth(prod.porcentaje).fillMaxHeight().background(Color(0xFF3B82F6), RoundedCornerShape(4.dp)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndicadorCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}
