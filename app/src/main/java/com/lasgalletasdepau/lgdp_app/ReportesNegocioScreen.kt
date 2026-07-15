package com.lasgalletasdepau.lgdp_app

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.admin.ReportesNegocioViewModel
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
        val pdfDocument = PdfDocument()
        val paint = Paint()
        var pageNumber = 1
        
        fun createNewPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            paint.textSize = 16f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("LAS GALLETAS DE PAU - REPORTE DE NEGOCIO", 297f, 40f, paint)
            
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Periodo: ${sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))} al ${sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))}", 50f, 60f, paint)
            canvas.drawText("Fecha de emisión: ${sdf.format(Date())}", 50f, 75f, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Página $pageNumber", 545f, 820f, paint)
            paint.textAlign = Paint.Align.LEFT
            
            canvas.drawLine(50f, 85f, 545f, 85f, paint)
            
            pageNumber++
            return page
        }

        var currentPage = createNewPage()
        var canvas = currentPage.canvas
        var y = 110f
        
        // --- RESUMEN GENERAL ---
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN GENERAL", 50f, y, paint)
        y += 20f
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        val resumen = listOf(
            "Ingresos Totales: S/ ${String.format(locale, "%.2f", totalIngresos)}",
            "Pedidos Pagados: $totalPedidos",
            "Pedidos Cancelados: $totalCancelados",
            "Egresos / Gastos Totales: S/ ${String.format(locale, "%.2f", totalEgresos)}",
            "Promedio de Ticket: S/ ${String.format(locale, "%.2f", promedioTicket)}"
        )
        
        resumen.forEach { text ->
            canvas.drawText("- $text", 60f, y, paint)
            y += 15f
        }
        
        y += 15f
        
        // --- VENTAS POR MÉTODO ---
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("VENTAS POR MÉTODO DE PAGO", 50f, y, paint)
        y += 20f
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        ventasPorMetodo.forEach { (metodo, monto) ->
            canvas.drawText("- $metodo: S/ ${String.format(locale, "%.2f", monto)}", 60f, y, paint)
            y += 15f
        }
        
        y += 20f
        
        // --- TOP PRODUCTOS ---
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("PRODUCTOS MÁS VENDIDOS", 50f, y, paint)
        y += 20f
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        topProductos.forEach { prod ->
            if (y > 780f) {
                pdfDocument.finishPage(currentPage)
                currentPage = createNewPage()
                canvas = currentPage.canvas
                y = 110f
            }
            canvas.drawText("- ${prod.nombre}: ${prod.cantidadVendida} unidades", 60f, y, paint)
            y += 15f
        }
        
        y += 20f
        
        // --- BOTTOM PRODUCTOS ---
        if (y > 750f) {
            pdfDocument.finishPage(currentPage)
            currentPage = createNewPage()
            canvas = currentPage.canvas
            y = 110f
        }
        
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("PRODUCTOS MENOS VENDIDOS", 50f, y, paint)
        y += 20f
        
        paint.isFakeBoldText = false
        paint.textSize = 10f
        bottomProductos.forEach { prod ->
            if (y > 780f) {
                pdfDocument.finishPage(currentPage)
                currentPage = createNewPage()
                canvas = currentPage.canvas
                y = 110f
            }
            canvas.drawText("- ${prod.nombre}: ${prod.cantidadVendida} unidades", 60f, y, paint)
            y += 15f
        }
        
        pdfDocument.finishPage(currentPage)
        val file = File(context.cacheDir, "Reporte_Negocio_Completo.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes del Negocio", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = { exportarReportePDF() }) {
                        Icon(Icons.Default.PictureAsPdf, "Exportar PDF", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E233D))
                
                error?.let {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(it, color = Color.Red, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicadorCard(Modifier.weight(1f), "Ingresos Totales", "S/ ${String.format(locale, "%.2f", totalIngresos)}", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF10B981))
                    IndicadorCard(Modifier.weight(1f), "Pedidos Pagados", "$totalPedidos", Icons.Default.Analytics, Color(0xFF1E233D))
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicadorCard(Modifier.weight(1f), "Pedidos Anulados", "$totalCancelados", Icons.Default.Cancel, Color.Red)
                    IndicadorCard(Modifier.weight(1f), "Egresos Totales", "S/ ${String.format(locale, "%.2f", totalEgresos)}", Icons.Default.Analytics, Color.DarkGray)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Resumen por Método de Pago", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        ventasPorMetodo.forEach { (metodo, monto) ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(metodo, style = MaterialTheme.typography.bodySmall)
                                Text("S/ ${String.format(locale, "%.2f", monto)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ticket Promedio:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text("S/ ${String.format(locale, "%.2f", promedioTicket)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("Productos más vendidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (topProductos.isEmpty() && !isLoading) {
                item { Text("No hay datos disponibles.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp)) }
            } else {
                items(topProductos.take(10)) { prod ->
                    ProductoBarra(prod, Color(0xFF3B82F6), locale)
                }
            }

            item {
                Text("Productos menos vendidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (bottomProductos.isEmpty() && !isLoading) {
                item { Text("No hay datos disponibles.", color = Color.Gray, modifier = Modifier.padding(start = 8.dp)) }
            } else {
                items(bottomProductos.take(10)) { prod ->
                    ProductoBarra(prod, Color(0xFFF59E0B), locale)
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ProductoBarra(prod: com.lasgalletasdepau.lgdp_app.ui.admin.ProductoEstadistica, color: Color, locale: Locale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(prod.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${prod.cantidadVendida} und.", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { prod.porcentaje },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = Color(0xFFE2E8F0),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
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
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
        }
    }
}
