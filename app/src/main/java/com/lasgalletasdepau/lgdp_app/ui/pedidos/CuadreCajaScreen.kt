package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.content.Intent
import android.graphics.pdf.PdfDocument
import com.lasgalletasdepau.lgdp_app.utils.PdfReportGenerator
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
import androidx.compose.foundation.isSystemInDarkTheme
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
    val billeteraDigital by viewModel.billeteraDigitalSistema.collectAsState()
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
    
    val totalSistema = efectivo + billeteraDigital + izipay
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
        val generator = PdfReportGenerator(context)
        generator.startNewPage("Reporte de Cierre de Caja")
        
        generator.addLabeledText("Fecha:", dateFormat.format(Date()))
        generator.addLabeledText("Cajero:", cajaSesion?.nombreCajero ?: "N/A")
        generator.addLabeledText("Apertura:", if(estaAbierta) timeFormat.format(Date(cajaSesion!!.fechaApertura)) else "N/A")
        generator.addHorizontalLine()
        
        generator.addSectionTitle("Resumen de Movimientos")
        generator.addRow(listOf("Monto de Apertura:", "S/ ${String.format(locale, "%.2f", montoAperturaVal)}"), listOf(1f, 1f))
        generator.addRow(listOf("Ingresos Efectivo:", "S/ ${String.format(locale, "%.2f", efectivo)}"), listOf(1f, 1f))
        generator.addRow(listOf("Ingresos Billetera Digital:", "S/ ${String.format(locale, "%.2f", billeteraDigital)}"), listOf(1f, 1f))
        generator.addRow(listOf("Ingresos Izipay:", "S/ ${String.format(locale, "%.2f", izipay)}"), listOf(1f, 1f))
        generator.addRow(listOf("Egresos / Gastos:", "S/ ${String.format(locale, "%.2f", egresosDouble)}"), listOf(1f, 1f))
        
        if (justificacionEgresos.isNotBlank()) {
            generator.addLabeledText("Justificación Egresos:", justificacionEgresos)
        }
        
        generator.addHorizontalLine()
        generator.addRow(listOf("TOTAL REGISTRADO EN SISTEMA:", "S/ ${String.format(locale, "%.2f", totalSistema)}"), listOf(1f, 1f), isHeader = true)
        
        generator.addSectionTitle("Verificación Física")
        generator.addRow(listOf("Efectivo Esperado (Físico):", "S/ ${String.format(locale, "%.2f", esperadoFisico)}"), listOf(1f, 1f))
        generator.addRow(listOf("Efectivo Real Contado:", "S/ ${String.format(locale, "%.2f", montoRealDouble)}"), listOf(1f, 1f))
        generator.addHorizontalLine()
        
        val colorDiferencia = if (abs(diferencia) < 0.01) android.graphics.Color.rgb(16, 185, 129) else android.graphics.Color.RED
        generator.addText("DIFERENCIA: S/ ${String.format(locale, "%.2f", diferencia)}", isBold = true, fontSize = 12f, color = colorDiferencia)
        
        if (justificacionDescuadre.isNotBlank()) {
            generator.addSectionTitle("Justificación de Descuadre")
            generator.addText(justificacionDescuadre)
        }
        
        val pdfDocument = generator.finish()
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estado: ${if(estaAbierta) "CAJA ABIERTA" else "CAJA CERRADA"}", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, 
                            color = if(estaAbierta) MaterialTheme.colorScheme.secondary else Color.Red
                        )
                        if (estaAbierta) {
                            Text(text = "Responsable: ${cajaSesion?.nombreCajero}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "Apertura: ${timeFormat.format(Date(cajaSesion!!.fechaApertura))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (!estaAbierta) {
                item {
                    Text(text = "Apertura de Turno", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), 
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
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Abrir Caja", fontWeight = FontWeight.Bold) }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Solo un cajero puede realizar la apertura.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                val esElCajeroResponsable = user?.id == cajaSesion?.usuarioCajeroId
                val esAdmin = user?.rol?.contains("Administrador") == true
                val puedeCerrar = esElCajeroResponsable || esAdmin

                item {
                    Text(text = "1. Configuración de Caja", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
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
                    Text(text = "2. Resumen de Ventas", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilaResumenCaja("Ventas en Efectivo", efectivo, locale)
                            FilaResumenCaja("Ventas Billetera Digital", billeteraDigital, locale)
                            FilaResumenCaja("Ventas Izipay", izipay, locale)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total registrado", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Text(String.format(locale, "S/. %.2f", totalSistema), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                item {
                    Text(text = "3. Verificación de Efectivo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "¿Cuál es el monto físico real en caja?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
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
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = puedeCerrar) { mostrarDialogoJustificacion = true }
                                ) {
                                    Text(
                                        text = if (justificacionDescuadre.isBlank()) "Diferencia detectada (S/. ${String.format(locale, "%.2f", diferencia)}). Toca para justificar." else "Justificación registrada correctamente.",
                                        color = MaterialTheme.colorScheme.secondary, 
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
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
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
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Justificar Descuadre", color = MaterialTheme.colorScheme.onSurface) },
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Guardar Justificación") } 
            }
        )
    }
}

@Composable
fun FilaResumenCaja(concepto: String, monto: Double, locale: Locale) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = concepto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = String.format(locale, "S/. %.2f", monto), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}
