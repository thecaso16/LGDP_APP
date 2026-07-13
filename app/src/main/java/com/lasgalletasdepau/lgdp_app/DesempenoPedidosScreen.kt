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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.DesempenoPedidosViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesempenoPedidosScreen(
    viewModel: DesempenoPedidosViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val stats by viewModel.estadisticasTrabajadores.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)) // Hace una semana
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    fun getCorrectedMillis(utcMillis: Long?): Long {
        if (utcMillis == null) return System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = utcMillis
        val localCalendar = Calendar.getInstance()
        localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        return localCalendar.timeInMillis
    }

    LaunchedEffect(datePickerStateStart.selectedDateMillis, datePickerStateEnd.selectedDateMillis) {
        viewModel.cargarEstadisticas(
            getCorrectedMillis(datePickerStateStart.selectedDateMillis),
            getCorrectedMillis(datePickerStateEnd.selectedDateMillis)
        )
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
                title = { Text("Desempeño de Equipo 🏆", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filtrar Desempeño", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Text("Ranking por Ventas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

            if (stats.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay datos para este periodo", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(stats) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Group, null, tint = Color(0xFF1E233D))
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("${item.cantidadPedidos} pedidos realizados", fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Text("S/ ${String.format(locale, "%.2f", item.totalVendido)}", fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Participación en ventas", fontSize = 11.sp, color = Color.Gray)
                                LinearProgressIndicator(
                                    progress = { item.porcentajeVentas },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 2.dp),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
