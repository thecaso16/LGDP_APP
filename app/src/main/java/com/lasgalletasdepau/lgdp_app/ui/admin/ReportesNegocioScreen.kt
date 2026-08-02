package com.lasgalletasdepau.lgdp_app.ui.admin

import android.content.Intent
import android.graphics.pdf.PdfDocument
import com.lasgalletasdepau.lgdp_app.utils.PdfReportGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.ReportesNegocioViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesNegocioScreen(
    viewModel: ReportesNegocioViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val totalIngresos by viewModel.totalIngresos.collectAsState()
    val totalPedidos by viewModel.totalPedidos.collectAsState()
    val totalCancelados by viewModel.totalCancelados.collectAsState()
    val totalEgresos by viewModel.totalEgresos.collectAsState()
    val topProductos by viewModel.topProductos.collectAsState()
    val bottomProductos by viewModel.bottomProductos.collectAsState()
    val ventasPorMetodo by viewModel.ventasPorMetodo.collectAsState()
    val promedioTicket by viewModel.promedioTicket.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

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

    fun exportarReportePDF() {
        val generator = PdfReportGenerator(context)
        generator.startNewPage("Reporte Estadístico del Negocio")
        
        generator.addLabeledText("Periodo:", "${sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))} al ${sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))}")
        generator.addLabeledText("Fecha de emisión:", sdf.format(Date()))
        generator.addHorizontalLine()
        
        generator.addSectionTitle("RESUMEN FINANCIERO")
        generator.addRow(listOf("Ingresos Totales:", "S/ ${String.format(locale, "%.2f", totalIngresos)}"), listOf(1f, 1f))
        generator.addRow(listOf("Egresos / Gastos Totales:", "S/ ${String.format(locale, "%.2f", totalEgresos)}"), listOf(1f, 1f))
        generator.addRow(listOf("Promedio de Ticket:", "S/ ${String.format(locale, "%.2f", promedioTicket)}"), listOf(1f, 1f))
        generator.addHorizontalLine()
        
        generator.addSectionTitle("MOVIMIENTO DE PEDIDOS")
        generator.addRow(listOf("Pedidos Pagados:", totalPedidos.toString()), listOf(1f, 1f))
        generator.addRow(listOf("Pedidos Cancelados:", totalCancelados.toString()), listOf(1f, 1f))
        
        generator.addSectionTitle("VENTAS POR MÉTODO DE PAGO")
        ventasPorMetodo.forEach { (metodo, monto) ->
            generator.addRow(listOf(metodo, "S/ ${String.format(locale, "%.2f", monto)}"), listOf(1f, 1f))
        }
        
        generator.addHorizontalLine()
        generator.addSectionTitle("PRODUCTOS MÁS VENDIDOS (TOP)")
        generator.addRow(listOf("Producto", "Cantidad Vendida"), listOf(3f, 1f), isHeader = true)
        topProductos.forEach { prod ->
            generator.addRow(listOf(prod.nombre, "${prod.cantidadVendida} und."), listOf(3f, 1f))
        }

        generator.addSectionTitle("PRODUCTOS MENOS VENDIDOS")
        generator.addRow(listOf("Producto", "Cantidad Vendida"), listOf(3f, 1f), isHeader = true)
        bottomProductos.forEach { prod ->
            generator.addRow(listOf(prod.nombre, "${prod.cantidadVendida} und."), listOf(3f, 1f))
        }

        val pdfDocument = generator.finish()
        val file = File(context.cacheDir, "Reporte_Negocio_Completo.pdf")
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
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reportes del Negocio",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Estadísticas y balance general:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = { exportarReportePDF() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, "Exportar PDF", tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                
                error?.let {
                    Surface(
                        color = Color(0xFFFFEBEE).copy(alpha = if(isSystemInDarkTheme()) 0.2f else 1f),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(it, color = if(isSystemInDarkTheme()) Color(0xFFFFCDD2) else Color.Red, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Filtrar por periodo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicadorCard(Modifier.weight(1f), "Ingresos Totales", "S/ ${String.format(locale, "%.2f", totalIngresos)}", Icons.AutoMirrored.Filled.TrendingUp, MaterialTheme.colorScheme.secondary)
                    IndicadorCard(Modifier.weight(1f), "Pedidos Pagados", "$totalPedidos", Icons.Default.Analytics, MaterialTheme.colorScheme.primary)
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicadorCard(Modifier.weight(1f), "Pedidos Anulados", "$totalCancelados", Icons.Default.Cancel, Color.Red)
                    IndicadorCard(Modifier.weight(1f), "Egresos Totales", "S/ ${String.format(locale, "%.2f", totalEgresos)}", Icons.Default.Analytics, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Resumen por Método de Pago", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        ventasPorMetodo.forEach { (metodo, monto) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(metodo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("S/ ${String.format(locale, "%.2f", monto)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ticket Promedio:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("S/ ${String.format(locale, "%.2f", promedioTicket)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            item {
                Text("Productos más vendidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (topProductos.isEmpty() && !isLoading) {
                item { Text("No hay datos disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp)) }
            } else {
                items(topProductos.take(10)) { prod ->
                    ProductoBarra(prod, if(isSystemInDarkTheme()) Color(0xFF60A5FA) else Color(0xFF3B82F6), locale)
                }
            }

            item {
                Text("Productos menos vendidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            if (bottomProductos.isEmpty() && !isLoading) {
                item { Text("No hay datos disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp)) }
            } else {
                items(bottomProductos.take(10)) { prod ->
                    ProductoBarra(prod, if(isSystemInDarkTheme()) Color(0xFFFBBF24) else Color(0xFFF59E0B), locale)
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProductoBarra(prod: ProductoEstadistica, color: Color, locale: Locale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(prod.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("${prod.cantidadVendida} und.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            LinearProgressIndicator(
                progress = { prod.porcentaje },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun IndicadorCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
        elevation = CardDefaults.cardElevation(2.dp), 
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
