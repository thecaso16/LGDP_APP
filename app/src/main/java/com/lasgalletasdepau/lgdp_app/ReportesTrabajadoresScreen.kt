package com.lasgalletasdepau.lgdp_app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CajaViewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoConDetalles
import com.lasgalletasdepau.lgdp_app.ui.pedidos.ReportesTrabajadoresViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesTrabajadoresScreen(
    onIrACierreCaja: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ReportesTrabajadoresViewModel = viewModel(),
    cajaViewModel: CajaViewModel = viewModel()
) {
    val context = LocalContext.current
    val historial by viewModel.historial.collectAsState()
    val usuario by viewModel.usuarioLogueado.collectAsState()
    val cajaAbierta by cajaViewModel.cajaSesion.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var selectedPedidoDetail by remember { mutableStateOf<PedidoConDetalles?>(null) }

    fun getCorrectedMillis(utcMillis: Long?): Long {
        if (utcMillis == null) return System.currentTimeMillis()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = utcMillis
        val localCalendar = Calendar.getInstance()
        localCalendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        return localCalendar.timeInMillis
    }

    LaunchedEffect(datePickerStateStart.selectedDateMillis, datePickerStateEnd.selectedDateMillis, usuario) {
        if (usuario != null) {
            val startStr = sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))
            val endStr = sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))
            viewModel.buscarPorRango(startStr, endStr)
        }
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

    val esCajeroResponsable = usuario?.uid != null && usuario?.uid == cajaAbierta?.usuarioCajeroId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Transacciones", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D)),
                navigationIcon = {
                    IconButton(onClick = onIrACierreCaja) {
                        Icon(Icons.Default.PointOfSale, "Cierre de Caja", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            if (historial.isNotEmpty() && esCajeroResponsable) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val startStr = sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))
                        val csvData = viewModel.generarCsvData()
                        val file = File(context.cacheDir, "Reporte_Ventas_${startStr.replace("/", "-")}.csv")
                        try {
                            FileOutputStream(file).use { it.write(csvData.toByteArray()) }
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Exportar Reporte Excel (CSV)"))
                        } catch (e: Exception) {}
                    },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar Reporte", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Rango de Búsqueda", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
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

            Text("Movimientos Encontrados (${historial.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

            if (historial.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay transacciones registradas.", color = Color.Gray) }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(historial) { item ->
                        val pedido = item.pedido
                        val timeSdf = remember(locale) { SimpleDateFormat("hh:mm a", locale) }
                        val hora = if (pedido.fecha != null) timeSdf.format(Date(pedido.fecha)) else "--:--"
                        val fechaStr = if (pedido.fecha != null) sdf.format(Date(pedido.fecha)) else ""

                        val statusColor = when (pedido.estado) {
                            EstadoPedido.PAGADO -> Color(0xFF10B981)
                            EstadoPedido.CANCELADO -> Color.Red
                            else -> Color(0xFFF59E0B)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White), 
                            shape = RoundedCornerShape(14.dp), 
                            elevation = CardDefaults.cardElevation(1.dp), 
                            modifier = Modifier.fillMaxWidth().clickable { selectedPedidoDetail = item }
                        ) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Pedido #${pedido.numeroPedido}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("$fechaStr | $hora", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = pedido.estado?.valor ?: "Sin estado", 
                                        color = statusColor, 
                                        fontWeight = FontWeight.Bold, 
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("S/ ${String.format(locale, "%.2f", pedido.total)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                    if (pedido.mesaId != null) {
                                        Text("Mesa ${pedido.mesaId}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    } else {
                                        Text("Para llevar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedPedidoDetail != null) {
        val p = selectedPedidoDetail!!.pedido
        val detalles = selectedPedidoDetail!!.detalles
        
        AlertDialog(
            onDismissRequest = { selectedPedidoDetail = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Detalle del Pedido #${p.numeroPedido}", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cliente:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(p.nombreCliente ?: "General", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estado:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                        Text(p.estado?.valor ?: "-", color = if(p.estado == EstadoPedido.CANCELADO) Color.Red else Color.Unspecified, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (p.metodoPago != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pago:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(p.metodoPago.valor, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    
                    Text("Productos:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    
                    detalles.forEach { d ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${d.cantidad}x ${d.nombreProducto}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text("S/ ${String.format(locale, "%.2f", d.precioUnitario * d.cantidad)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                        Text("S/ ${String.format(locale, "%.2f", p.total)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = Color(0xFF1E233D))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPedidoDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
