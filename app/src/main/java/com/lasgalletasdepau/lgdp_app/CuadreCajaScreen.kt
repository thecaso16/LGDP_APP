package com.lasgalletasdepau.lgdp_app

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.CajaViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadreCajaScreen(
    onRegresar: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CajaViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.usuarioLogueado.collectAsState()
    val cajaSesion by viewModel.cajaSesion.collectAsState()
    
    val efectivo by viewModel.efectivoSistema.collectAsState()
    val yape by viewModel.yapeSistema.collectAsState()
    val izipay by viewModel.izipaySistema.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    val timeFormat = remember(locale) { SimpleDateFormat("hh:mm a", locale) }

    val montoAperturaInput by viewModel.montoApertura.collectAsState()
    val egresos by viewModel.egresos.collectAsState()
    val justificacionEgresos by viewModel.justificacionEgresos.collectAsState()
    val montoRealInput by viewModel.montoRealFisico.collectAsState()

    var justificacionDescuadre by remember { mutableStateOf("") }
    var mostrarDialogoJustificacion by remember { mutableStateOf(false) }
    
    val totalSistema = efectivo + yape + izipay
    val esCajero by viewModel.esCajero.collectAsState()
    val estaAbierta = cajaSesion != null

    val montoAperturaVal = if (estaAbierta) cajaSesion!!.montoApertura else (montoAperturaInput.toDoubleOrNull() ?: 0.0)
    val egresosDouble = egresos.toDoubleOrNull() ?: 0.0
    val montoRealDouble = montoRealInput.toDoubleOrNull() ?: 0.0
    
    val esperadoFisico = montoAperturaVal + efectivo - egresosDouble
    val diferencia = montoRealDouble - esperadoFisico

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun exportarCierrePDF() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        
        var y = 50f
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("REPORTE DE CIERRE DE CAJA", 50f, y, paint)
        
        y += 40f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Fecha: ${dateFormat.format(Date())}", 50f, y, paint)
        y += 15f
        canvas.drawText("Cajero: ${cajaSesion?.nombreCajero ?: "N/A"}", 50f, y, paint)
        y += 15f
        canvas.drawText("Apertura: ${if(estaAbierta) timeFormat.format(Date(cajaSesion!!.fechaApertura)) else "N/A"}", 50f, y, paint)
        
        y += 40f
        paint.isFakeBoldText = true
        canvas.drawText("RESUMEN DE MOVIMIENTOS", 50f, y, paint)
        y += 25f
        paint.isFakeBoldText = false
        canvas.drawText("Monto de Apertura:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", montoAperturaVal)}", 400f, y, paint)
        y += 20f
        canvas.drawText("Ingresos Efectivo:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", efectivo)}", 400f, y, paint)
        y += 20f
        canvas.drawText("Ingresos Billetera Digital:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", yape)}", 400f, y, paint)
        y += 20f
        canvas.drawText("Ingresos Izipay:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", izipay)}", 400f, y, paint)
        y += 20f
        canvas.drawText("Egresos / Gastos:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", egresosDouble)}", 400f, y, paint)
        
        if (justificacionEgresos.isNotBlank()) {
            y += 20f
            canvas.drawText("Justificación Egresos:", 60f, y, paint)
            y += 15f
            paint.textSize = 10f
            canvas.drawText(justificacionEgresos, 70f, y, paint)
            paint.textSize = 12f
            y += 10f
        }
        
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL REGISTRADO EN SISTEMA:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", totalSistema)}", 400f, y, paint)
        
        y += 40f
        canvas.drawText("VERIFICACIÓN FÍSICA", 50f, y, paint)
        y += 25f
        paint.isFakeBoldText = false
        canvas.drawText("Efectivo Esperado (Físico):", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", esperadoFisico)}", 400f, y, paint)
        y += 20f
        canvas.drawText("Efectivo Real Contado:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", montoRealDouble)}", 400f, y, paint)
        y += 25f
        paint.isFakeBoldText = true
        canvas.drawText("DIFERENCIA:", 60f, y, paint)
        canvas.drawText("S/ ${String.format(locale, "%.2f", diferencia)}", 400f, y, paint)
        
        if (justificacionDescuadre.isNotBlank()) {
            y += 40f
            canvas.drawText("JUSTIFICACIÓN:", 50f, y, paint)
            y += 20f
            paint.isFakeBoldText = false
            canvas.drawText(justificacionDescuadre, 60f, y, paint)
        }
        
        pdfDocument.finishPage(page)
        val file = File(context.cacheDir, "Cierre_Caja_Actual.pdf")
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Gestión de Caja",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E233D)
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233D).copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estado: ${if(estaAbierta) "CAJA ABIERTA" else "CAJA CERRADA"}", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, 
                            color = if(estaAbierta) Color(0xFF10B981) else Color.Red
                        )
                        if (estaAbierta) {
                            Text(text = "Responsable: ${cajaSesion?.nombreCajero}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Text(text = "Apertura: ${timeFormat.format(Date(cajaSesion!!.fechaApertura))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            if (!estaAbierta) {
                item {
                    Text(text = "Apertura de Turno", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                        colors = CardDefaults.cardColors(containerColor = Color.White), 
                        shape = RoundedCornerShape(16.dp), 
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (esCajero) {
                                OutlinedTextField(
                                    value = montoAperturaInput,
                                    onValueChange = { viewModel.montoApertura.value = it },
                                    label = { Text("Monto de apertura (S/.)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = { viewModel.abrirCaja() },
                                    enabled = montoAperturaInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Abrir Caja", fontWeight = FontWeight.Bold) }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Solo un cajero puede realizar la apertura.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                val esElCajeroResponsable = user?.uid == cajaSesion?.usuarioCajeroId
                val esAdmin = user?.rol?.contains("Administrador") == true
                val puedeCerrar = esElCajeroResponsable || esAdmin

                item {
                    Text(text = "1. Configuración de Caja", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = String.format(locale, "S/. %.2f", cajaSesion!!.montoApertura),
                                onValueChange = {},
                                label = { Text("Monto inicial") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = egresos,
                                onValueChange = { if(puedeCerrar) viewModel.egresos.value = it },
                                label = { Text("Egresos y gastos (S/.)") },
                                readOnly = !puedeCerrar,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            if (egresosDouble > 0) {
                                OutlinedTextField(
                                    value = justificacionEgresos,
                                    onValueChange = { if(puedeCerrar) viewModel.justificacionEgresos.value = it },
                                    label = { Text("Justificación de egresos") },
                                    placeholder = { Text("Ej. Compra de suministros...") },
                                    readOnly = !puedeCerrar,
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(text = "2. Resumen de Ventas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilaResumenCaja("Ventas en Efectivo", efectivo, locale)
                            FilaResumenCaja("Ventas Billetera Digital", yape, locale)
                            FilaResumenCaja("Ventas Izipay", izipay, locale)
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total registrado", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                                Text(String.format(locale, "S/. %.2f", totalSistema), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            }
                        }
                    }
                }

                item {
                    Text(text = "3. Verificación de Efectivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "¿Cuál es el monto físico real en caja?", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = montoRealInput,
                                onValueChange = { if(puedeCerrar) viewModel.montoRealFisico.value = it },
                                placeholder = { Text("0.00") },
                                readOnly = !puedeCerrar,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (diferencia != 0.0 && montoRealInput.isNotBlank()) {
                                Surface(
                                    color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = puedeCerrar) { mostrarDialogoJustificacion = true }
                                ) {
                                    Text(
                                        text = if (justificacionDescuadre.isBlank()) "Diferencia detectada (S/. ${String.format(locale, "%.2f", diferencia)}). Toca para justificar." else "Justificación registrada correctamente.",
                                        color = Color(0xFF1D4ED8), 
                                        style = MaterialTheme.typography.bodySmall, 
                                        fontWeight = FontWeight.Bold, 
                                        textAlign = TextAlign.Center, 
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (puedeCerrar) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { exportarCierrePDF() },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) { 
                                Icon(Icons.Default.PictureAsPdf, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Generar PDF de Cierre", fontWeight = FontWeight.Bold) 
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val exito = viewModel.finalizarCierre(justificacionDescuadre)
                                        if (exito) {
                                            onRegresar()
                                        } else {
                                            snackbarHostState.showSnackbar("Error al procesar el cierre. Verifique su conexión.")
                                        }
                                    }
                                },
                                enabled = montoRealInput.isNotBlank() && 
                                          (abs(diferencia) < 0.01 || justificacionDescuadre.isNotBlank()) &&
                                          (egresosDouble == 0.0 || justificacionEgresos.isNotBlank()),
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                            ) { Text("Finalizar Turno", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (mostrarDialogoJustificacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoJustificacion = false },
            title = { Text("Justificar Descuadre") },
            text = {
                OutlinedTextField(
                    value = justificacionDescuadre,
                    onValueChange = { justificacionDescuadre = it },
                    placeholder = { Text("Ej. Error en cobro mesa 5...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = { 
                Button(
                    onClick = { mostrarDialogoJustificacion = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) { Text("Guardar Justificación") } 
            }
        )
    }
}

@Composable
fun FilaResumenCaja(concepto: String, monto: Double, locale: Locale) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = concepto, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
        Text(text = String.format(locale, "S/. %.2f", monto), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E233D))
    }
}
