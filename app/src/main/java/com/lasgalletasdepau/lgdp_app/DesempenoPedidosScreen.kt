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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
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
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.ui.admin.DesempenoPedidosViewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoConDetalles
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
            canvas.drawText("LAS GALLETAS DE PAU - DESEMPEÑO DE EQUIPO", 297f, 40f, paint)
            
            paint.textSize = 10f
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Periodo evaluado: ${sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))} al ${sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))}", 50f, 60f, paint)
            canvas.drawText("Fecha de reporte: ${sdf.format(Date())}", 50f, 75f, paint)
            
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
        
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("RANKING DE PRODUCTIVIDAD", 50f, y, paint)
        y += 25f
        
        stats.forEach { item ->
            if (y > 750f) {
                pdfDocument.finishPage(currentPage)
                currentPage = createNewPage()
                canvas = currentPage.canvas
                y = 110f
            }
            
            paint.isFakeBoldText = true
            paint.textSize = 11f
            canvas.drawText(item.nombre, 60f, y, paint)
            y += 18f
            
            paint.isFakeBoldText = false
            paint.textSize = 10f
            canvas.drawText("  - Pedidos registrados: ${item.cantidadPedidos}", 60f, y, paint)
            y += 15f
            canvas.drawText("  - Ventas totales: S/ ${String.format(locale, "%.2f", item.totalVendido)}", 60f, y, paint)
            y += 15f
            canvas.drawText("  - Participación: ${String.format(locale, "%.1f", item.porcentajeVentas * 100)}%", 60f, y, paint)
            y += 25f
            
            canvas.drawLine(60f, y - 5f, 535f, y - 5f, paint)
            y += 15f
        }
        
        pdfDocument.finishPage(currentPage)

        val file = File(context.cacheDir, "Reporte_Desempeno_Global.pdf")
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
                title = { Text("Desempeño de Equipo", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = { exportarPDFDesempeno() }) {
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC))) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Color(0xFF1E233D))
            }

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
                modifier = Modifier.fillMaxWidth().padding(16.dp), 
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Filtrar periodo de evaluación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (stats.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(8.dp))
                        Text("No se encontraron datos en el periodo seleccionado.", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), 
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(stats) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                trabajadorSeleccionadoId = item.usuarioId
                                viewModel.cargarPedidosTrabajador(
                                    item.usuarioId,
                                    getCorrectedMillis(datePickerStateStart.selectedDateMillis),
                                    getCorrectedMillis(datePickerStateEnd.selectedDateMillis)
                                )
                            },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF1E233D).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Group, null, tint = Color(0xFF1E233D))
                                        }
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(item.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                        Text("${item.cantidadPedidos} pedidos registrados", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    Text("S/ ${String.format(locale, "%.2f", item.totalVendido)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Participación en ventas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Text("${String.format(locale, "%.1f", item.porcentajeVentas * 100)}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    LinearProgressIndicator(
                                        progress = { item.porcentajeVentas },
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

    if (trabajadorSeleccionadoId != null) {
        val trabajador = stats.find { it.usuarioId == trabajadorSeleccionadoId }
        AlertDialog(
            onDismissRequest = { trabajadorSeleccionadoId = null; viewModel.limpiarPedidosTrabajador() },
            title = { Text("Pedidos de ${trabajador?.nombre ?: ""}", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (pedidosTrabajador.isEmpty()) {
                        Text("No se encontraron pedidos.", modifier = Modifier.padding(16.dp))
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(pedidosTrabajador) { pcd ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { pedidoSeleccionadoDeta = pcd },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("Pedido #${pcd.pedido.numeroPedido}", fontWeight = FontWeight.Bold)
                                            Text("${sdf.format(Date(pcd.pedido.fecha ?: 0))} | ${timeSdf.format(Date(pcd.pedido.fecha ?: 0))}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Text("S/ ${String.format(locale, "%.2f", pcd.pedido.total)}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { trabajadorSeleccionadoId = null; viewModel.limpiarPedidosTrabajador() }) { Text("Cerrar") } }
        )
    }

    if (pedidoSeleccionadoDeta != null) {
        val p = pedidoSeleccionadoDeta!!.pedido
        val detalles = pedidoSeleccionadoDeta!!.detalles
        AlertDialog(
            onDismissRequest = { pedidoSeleccionadoDeta = null },
            title = { Text("Detalle de Pedido #${p.numeroPedido}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cliente: ${p.nombreCliente ?: "General"}")
                    Text("Estado: ${p.estado?.valor ?: "-"}")
                    if (p.metodoPago != null) Text("Pago: ${p.metodoPago.valor}")
                    if (p.estado == EstadoPedido.CANCELADO && !p.notas.isNullOrBlank()) {
                        Text("Motivo cancelación: ${p.notas}", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    detalles.forEach { d ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${d.cantidad}x ${d.nombreProducto}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text("S/ ${String.format(locale, "%.2f", d.precioUnitario * d.cantidad)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.Black)
                        Text("S/ ${String.format(locale, "%.2f", p.total)}", fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pedidoSeleccionadoDeta = null }) { Text("Cerrar") } }
        )
    }
}
