package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.content.Intent
import com.lasgalletasdepau.lgdp_app.utils.PdfReportGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.PedidoConDetalles
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
    val modo by viewModel.modo.collectAsState()
    val usuario by viewModel.usuarioLogueado.collectAsState()
    val cajaAbierta by cajaViewModel.cajaSesion.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    val timeSdf = remember(locale) { SimpleDateFormat("hh:mm a", locale) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val datePickerStateStart = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    val datePickerStateEnd = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    var selectedPedidoDetail by remember { mutableStateOf<PedidoConDetalles?>(null) }

    fun getCorrectedMillis(utcMillis: Long?): Long {
        if (utcMillis == null) return System.currentTimeMillis()
        val calendarUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendarUtc.timeInMillis = utcMillis
        val localCalendar = Calendar.getInstance()
        localCalendar.set(calendarUtc.get(Calendar.YEAR), calendarUtc.get(Calendar.MONTH), calendarUtc.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        return localCalendar.timeInMillis
    }

    LaunchedEffect(modo, datePickerStateStart.selectedDateMillis, datePickerStateEnd.selectedDateMillis, usuario) {
        if (usuario != null) {
            if (modo == ModoHistorial.TURNO_ACTUAL) {
                viewModel.cargarPedidosTurnoActual()
            } else {
                val startStr = sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))
                val endStr = sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))
                viewModel.buscarPorRango(startStr, endStr)
            }
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

    fun exportarHistorialPDF() {
        try {
            val generator = PdfReportGenerator(context)
            generator.startNewPage("Historial de Ventas")
            
            val periodoTexto = if (modo == ModoHistorial.TURNO_ACTUAL) {
                "Turno Actual (desde ${cajaAbierta?.fechaApertura?.let { sdf.format(Date(it)) } ?: "-"})"
            } else {
                "${sdf.format(Date(getCorrectedMillis(datePickerStateStart.selectedDateMillis)))} al ${sdf.format(Date(getCorrectedMillis(datePickerStateEnd.selectedDateMillis)))}"
            }

            generator.addLabeledText("Generado por:", "${usuario?.nombres} ${usuario?.apellidos}")
            generator.addLabeledText("Periodo:", periodoTexto)
            generator.addHorizontalLine()

            generator.addRow(listOf("Nº Orden", "Fecha/Hora", "Cliente", "Mesa", "Total"), listOf(1.5f, 2.5f, 2f, 1.5f, 1.5f), isHeader = true)
            generator.addHorizontalLine()

            historial.forEach { item ->
                val p = item.pedido
                val fechaStr = if (p.fecha != null) sdf.format(Date(p.fecha)) else "-"
                val horaStr = if (p.fecha != null) timeSdf.format(Date(p.fecha)) else "-"
                
                generator.addRow(
                    listOf(
                        "#${p.numeroPedido}",
                        "$fechaStr $horaStr",
                        p.nombreCliente ?: "General",
                        if (p.mesaId != null) "Mesa ${p.mesaId}" else "Llevar",
                        "S/ ${String.format(locale, "%.2f", p.total)}"
                    ),
                    listOf(1.5f, 2.5f, 2f, 1.5f, 1.5f)
                )

                // Atendido por
                generator.addText("      Atendido por: ${p.usuarioNombre ?: "Sistema"}", fontSize = 8f, color = android.graphics.Color.GRAY)

                // Detalle de productos en tamaño pequeño
                item.detalles.forEach { d ->
                    generator.addText("      • ${d.cantidad}x ${d.nombreProducto} (S/ ${String.format(locale, "%.2f", d.precioUnitario)})", fontSize = 8f, color = android.graphics.Color.DKGRAY, spaceAfter = 12f)
                }
                generator.addHorizontalLine()
            }

            val pdfDocument = generator.finish()
            val fileName = "Historial_Ventas_${System.currentTimeMillis()}.pdf"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, 
                "${context.packageName}.fileprovider", 
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, "Abrir reporte PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Podrías mostrar un Toast aquí si tienes acceso al context
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (modo == ModoHistorial.TURNO_ACTUAL) "Pedidos del Turno" else "Búsqueda Histórica",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (modo == ModoHistorial.TURNO_ACTUAL) "Ventas totales de la caja abierta" else "Consulta ventas de días anteriores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (modo == ModoHistorial.BUSQUEDA_HISTORICA) {
                IconButton(onClick = { viewModel.cambiarModo(ModoHistorial.TURNO_ACTUAL) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (modo == ModoHistorial.TURNO_ACTUAL) {
            Button(
                onClick = { viewModel.cambiarModo(ModoHistorial.BUSQUEDA_HISTORICA) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.width(8.dp))
                Text("Ver historial de días anteriores")
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Filtrar periodo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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

        // Botón de exportación (siempre visible o según prefieras)
        Button(
            onClick = { exportarHistorialPDF() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, null)
            Spacer(Modifier.width(8.dp))
            Text(if (modo == ModoHistorial.TURNO_ACTUAL) "Exportar ventas del turno" else "Exportar búsqueda")
        }

        Text(
            text = if (modo == ModoHistorial.TURNO_ACTUAL) "Ventas del Turno (${historial.size})" else "Resultados de Búsqueda (${historial.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (historial.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay transacciones registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                items(historial) { item ->
                    val pedido = item.pedido
                    val hora = if (pedido.fecha != null) timeSdf.format(Date(pedido.fecha)) else "--:--"
                    val fechaStr = if (pedido.fecha != null) sdf.format(Date(pedido.fecha)) else ""

                    val statusColor = when (pedido.estado) {
                        EstadoPedido.PAGADO -> MaterialTheme.colorScheme.secondary
                        EstadoPedido.CANCELADO -> Color.Red
                        else -> Color(0xFFF59E0B)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
                        shape = RoundedCornerShape(14.dp), 
                        elevation = CardDefaults.cardElevation(1.dp), 
                        modifier = Modifier.fillMaxWidth().clickable { selectedPedidoDetail = item }
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pedido #${pedido.numeroPedido}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text("$fechaStr | $hora", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Atendido por: ${pedido.usuarioNombre ?: "Sistema"}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = pedido.estado?.valor ?: "Sin estado", 
                                    color = statusColor, 
                                    fontWeight = FontWeight.Bold, 
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("S/ ${String.format(locale, "%.2f", pedido.total)}", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                if (pedido.mesaId != null) {
                                    Text("Mesa ${pedido.mesaId}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    Text("Para llevar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Detalle del Pedido #${p.numeroPedido}", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Atendido por:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(p.usuarioNombre ?: "Sistema", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cliente:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(p.nombreCliente ?: "General", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estado:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text(p.estado?.valor ?: "-", color = if(p.estado == EstadoPedido.CANCELADO) Color.Red else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (p.metodoPago != null) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pago:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(p.metodoPago.valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Text("Productos:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    
                    detalles.forEach { d ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${d.cantidad}x ${d.nombreProducto}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            Text("S/ ${String.format(locale, "%.2f", d.precioUnitario * d.cantidad)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("S/ ${String.format(locale, "%.2f", p.total)}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPedidoDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        )
    }
}
