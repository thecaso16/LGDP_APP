package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Analytics
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
import androidx.compose.ui.text.style.TextAlign
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
    val error by viewModel.error.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Rango predeterminado: desde el inicio del mes actual hasta hoy
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonthMillis = calendar.timeInMillis

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = startOfMonthMillis)
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = todayMillis)

    fun getCorrectedMillis(utcMillis: Long?): Long {
        if (utcMillis == null) return System.currentTimeMillis()
        val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendarUtc.timeInMillis = utcMillis
        val localCalendar = Calendar.getInstance()
        localCalendar.set(calendarUtc.get(Calendar.YEAR), calendarUtc.get(Calendar.MONTH), calendarUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
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
                title = { Text("Reportes del Negocio", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
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
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E233D))
            }

            error?.let {
                Surface(
                    color = Color(0xFFFFEBEE),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(it, color = Color.Red, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Filtrar por periodo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis))),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Desde") },
                            trailingIcon = { IconButton(onClick = { showStartDatePicker = true }) { Icon(Icons.Default.CalendarMonth, null) } },
                            modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis))),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hasta") },
                            trailingIcon = { IconButton(onClick = { showEndDatePicker = true }) { Icon(Icons.Default.CalendarMonth, null) } },
                            modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IndicadorCard(Modifier.weight(1f), "Ingresos Totales", "S/ ${String.format(locale, "%.2f", totalIngresos)}", Icons.Default.TrendingUp, Color(0xFF10B981))
                IndicadorCard(Modifier.weight(1f), "Pedidos Pagados", "$totalPedidos", Icons.Default.DateRange, Color(0xFF1E233D))
            }

            Text(
                "Top Productos Vendidos", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(16.dp)
            )

            if (topProductos.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("No se encontraron ventas para este periodo.", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(topProductos) { prod ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(prod.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    Text("${prod.cantidadVendida} und.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                LinearProgressIndicator(
                                    progress = { prod.porcentaje },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFFE2E8F0),
                                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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
fun IndicadorCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = Color.White), 
        elevation = CardDefaults.cardElevation(2.dp), 
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
        }
    }
}
