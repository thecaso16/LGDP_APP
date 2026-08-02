package com.lasgalletasdepau.lgdp_app.ui.admin

import android.content.Intent
import com.lasgalletasdepau.lgdp_app.utils.PdfReportGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.ui.admin.DesempenoPedidosViewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoConDetalles
import androidx.compose.foundation.isSystemInDarkTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesempenoPedidosScreen(
    viewModel: DesempenoPedidosViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.estadisticasTrabajadores.collectAsState()
    val pedidosTrabajador by viewModel.pedidosTrabajadorSeleccionado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    val timeSdf = remember(locale) { SimpleDateFormat("hh:mm a", locale) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    var trabajadorSeleccionadoId by remember { mutableStateOf<String?>(null) }
    var pedidoSeleccionadoDeta by remember { mutableStateOf<PedidoConDetalles?>(null) }

    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val lastWeekMillis = calendar.timeInMillis

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = lastWeekMillis)
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

    fun exportarPDFDesempeno() {
        val generator = PdfReportGenerator(context)
        generator.startNewPage("Reporte de Desempeño de Equipo")
        
        generator.addLabeledText("Periodo evaluado:", "${sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))} al ${sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))}")
        generator.addLabeledText("Fecha de reporte:", sdf.format(Date()))
        generator.addHorizontalLine()
        
        generator.addSectionTitle("RANKING DE PRODUCTIVIDAD")
        generator.addRow(listOf("Trabajador", "Pedidos", "Total Ventas", "Participación"), listOf(3f, 1.5f, 2f, 1.5f), isHeader = true)
        generator.addHorizontalLine()

        stats.forEach { item ->
            generator.addRow(
                listOf(
                    item.nombre,
                    item.cantidadPedidos.toString(),
                    "S/ ${String.format(locale, "%.2f", item.totalVendido)}",
                    "${String.format(locale, "%.1f", item.porcentajeVentas * 100)}%"
                ),
                listOf(3f, 1.5f, 2f, 1.5f)
            )
        }
        
        generator.addHorizontalLine()
        generator.addText("Resumen final del equipo:", isBold = true)
        generator.addText("Total de pedidos en el periodo: ${stats.sumOf { it.cantidadPedidos }}")
        generator.addText("Ventas totales acumuladas: S/ ${String.format(locale, "%.2f", stats.sumOf { it.totalVendido })}")

        val pdfDocument = generator.finish()
        val file = File(context.cacheDir, "Reporte_Desempeno_Global.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Desempeño de Equipo",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Productividad y ventas por trabajador:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { exportarPDFDesempeno() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "Exportar PDF", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
                }

                error?.let {
                    Surface(
                        color = Color(0xFFFFEBEE).copy(alpha = if(isSystemInDarkTheme()) 0.2f else 1f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(it, color = if(isSystemInDarkTheme()) Color(0xFFFFCDD2) else Color.Red, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Filtrar periodo de evaluación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Text(
                    "Productividad por trabajador", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            if (stats.isEmpty() && !isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("No se encontraron datos.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(stats) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable {
                            trabajadorSeleccionadoId = item.usuarioId
                            viewModel.cargarPedidosTrabajador(
                                item.usuarioId,
                                getCorrectedMillis(datePickerStateStart.selectedDateMillis),
                                getCorrectedMillis(datePickerStateEnd.selectedDateMillis)
                            )
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("${item.cantidadPedidos} pedidos registrados", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("S/ ${String.format(locale, "%.2f", item.totalVendido)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Participación en ventas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${String.format(locale, "%.1f", item.porcentajeVentas * 100)}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                LinearProgressIndicator(
                                    progress = { item.porcentajeVentas },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = if(isSystemInDarkTheme()) Color(0xFF60A5FA) else Color(0xFF3B82F6),
                                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (trabajadorSeleccionadoId != null) {
        val trabajador = stats.find { it.usuarioId == trabajadorSeleccionadoId }
        AlertDialog(
            onDismissRequest = { trabajadorSeleccionadoId = null; viewModel.limpiarPedidosTrabajador() },
            title = { Text("Pedidos de ${trabajador?.nombre ?: ""}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (pedidosTrabajador.isEmpty()) {
                        Text("No hay pedidos registrados en este periodo.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(pedidosTrabajador) { pcd ->
                                val isCancelado = pcd.pedido.estado == EstadoPedido.CANCELADO
                                val accentColor = if (isCancelado) Color.Red else MaterialTheme.colorScheme.secondary
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { pedidoSeleccionadoDeta = pcd },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        Modifier.padding(16.dp), 
                                        horizontalArrangement = Arrangement.SpaceBetween, 
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Orden #${pcd.pedido.numeroPedido}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                            Text("${sdf.format(Date(pcd.pedido.fecha ?: 0))} • ${timeSdf.format(Date(pcd.pedido.fecha ?: 0))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(Modifier.height(4.dp))
                                            Surface(
                                                color = accentColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = pcd.pedido.estado?.valor ?: "-", 
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall, 
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor
                                                )
                                            }
                                        }
                                        Text(
                                            text = "S/ ${String.format(locale, "%.2f", pcd.pedido.total)}", 
                                            fontWeight = FontWeight.Black, 
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isCancelado) Color.Red else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                Button(
                    onClick = { trabajadorSeleccionadoId = null; viewModel.limpiarPedidosTrabajador() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cerrar", color = Color.White) }
            }
        )
    }

    if (pedidoSeleccionadoDeta != null) {
        val p = pedidoSeleccionadoDeta!!.pedido
        val detalles = pedidoSeleccionadoDeta!!.detalles
        val isCancelado = p.estado == EstadoPedido.CANCELADO
        
        AlertDialog(
            onDismissRequest = { pedidoSeleccionadoDeta = null },
            title = { Text("Detalle de Pedido #${p.numeroPedido}", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cliente:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(p.nombreCliente ?: "General", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estado:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Text(p.estado?.valor ?: "-", color = if(isCancelado) Color.Red else MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                    if (p.metodoPago != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pago:", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            Text(p.metodoPago.valor, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    if (isCancelado && !p.notas.isNullOrBlank()) {
                        Surface(color = Color.Red.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Text("Motivo: ${p.notas}", color = Color.Red, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Text("Productos", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    
                    detalles.forEach { d ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${d.cantidad}x ${d.nombreProducto}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("S/ ${String.format(locale, "%.2f", d.precioUnitario * d.cantidad)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("S/ ${String.format(locale, "%.2f", p.total)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = if(isCancelado) Color.Red else MaterialTheme.colorScheme.secondary)
                    }
                }
            },
            confirmButton = { 
                Button(
                    onClick = { pedidoSeleccionadoDeta = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Entendido", color = Color.White) }
            }
        )
    }
}
