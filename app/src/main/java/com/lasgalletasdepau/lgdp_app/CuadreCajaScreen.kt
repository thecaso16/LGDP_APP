package com.lasgalletasdepau.lgdp_app

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
fun CuadreCajaScreen(onRegresar: () -> Unit, viewModel: CajaViewModel = viewModel()) {
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
    val montoRealInput by viewModel.montoRealFisico.collectAsState()

    var justificacionDescuadre by remember { mutableStateOf("") }
    var mostrarDialogoJustificacion by remember { mutableStateOf(false) }
    
    val totalSistema = efectivo + yape + izipay
    val esCajero = viewModel.tieneRolCajero()
    val estaAbierta = cajaSesion != null

    val montoAperturaVal = if (estaAbierta) cajaSesion!!.montoApertura else (montoAperturaInput.toDoubleOrNull() ?: 0.0)
    val egresosDouble = egresos.toDoubleOrNull() ?: 0.0
    val montoRealDouble = montoRealInput.toDoubleOrNull() ?: 0.0
    
    val esperadoFisico = montoAperturaVal + efectivo - egresosDouble
    val diferencia = montoRealDouble - esperadoFisico

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Caja", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
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
                .background(Color(0xFFF8FAFC))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233D).copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Estado: ${if(estaAbierta) "CAJA ABIERTA ✅" else "CAJA CERRADA 🔒"}", fontWeight = FontWeight.Bold, color = if(estaAbierta) Color(0xFF10B981) else Color.Red)
                        if (estaAbierta) {
                            Text(text = "Responsable: ${cajaSesion?.nombreCajero}", fontSize = 14.sp, color = Color.Gray)
                            Text(text = "Desde: ${timeFormat.format(Date(cajaSesion!!.fechaApertura))}", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }

            if (!estaAbierta) {
                // Pantalla de APERTURA
                item {
                    Text(text = "Apertura de Turno", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (esCajero) {
                                OutlinedTextField(
                                    value = montoAperturaInput,
                                    onValueChange = { viewModel.montoApertura.value = it },
                                    label = { Text("Monto de Apertura (S/.)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = { viewModel.abrirCaja() },
                                    enabled = montoAperturaInput.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                                ) { Text("Abrir Nueva Caja", fontWeight = FontWeight.Bold) }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Solo un Cajero puede aperturar el turno.", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            } else {
                // Pantalla de CIERRE
                val esElCajeroResponsable = user?.uid == cajaSesion?.usuarioCajeroId

                item {
                    Text(text = "1. Configuración de Caja", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = String.format("S/. %.2f", cajaSesion!!.montoApertura),
                                onValueChange = {},
                                label = { Text("Monto de Apertura") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = egresos,
                                onValueChange = { if(esElCajeroResponsable) viewModel.egresos.value = it },
                                label = { Text("Egresos / Salidas (S/.)") },
                                readOnly = !esElCajeroResponsable,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                item {
                    Text(text = "2. Ventas del Turno (Sistema)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilaResumenCaja("Ventas Efectivo", efectivo)
                            FilaResumenCaja("Ventas Yape/Plin", yape)
                            FilaResumenCaja("Ventas Izipay", izipay)
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Ventas", fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                                Text(String.format("S/. %.2f", totalSistema), fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            }
                        }
                    }
                }

                item {
                    Text(text = "3. Verificación de Efectivo Físico", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "¿Cuánto efectivo real hay en caja?", fontSize = 14.sp, color = Color(0xFF475569))
                            OutlinedTextField(
                                value = montoRealInput,
                                onValueChange = { if(esElCajeroResponsable) viewModel.montoRealFisico.value = it },
                                placeholder = { Text("S/. 0.00") },
                                readOnly = !esElCajeroResponsable,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (diferencia != 0.0 && montoRealInput.isNotBlank()) {
                                Surface(
                                    color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clickable(enabled = esElCajeroResponsable) { mostrarDialogoJustificacion = true }
                                ) {
                                    Text(
                                        text = if (justificacionDescuadre.isBlank()) "⚠️ Descuadre detectado (S/. ${String.format("%.2f", diferencia)}). Toca para justificar." else "📝 Justificación añadida",
                                        color = Color(0xFF1D4ED8), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (esElCajeroResponsable) {
                    item {
                        Button(
                            onClick = {
                                val csv = "Concepto;Monto\nApertura;${cajaSesion?.montoApertura}\nEgresos;${egresos}\nEfectivo;${efectivo}\nYape;${yape}\nIzipay;${izipay}\nEsperado;${esperadoFisico}\nReal;${montoRealDouble}\nDiferencia;${diferencia}"
                                val file = File(context.cacheDir, "Cierre_${dateFormat.format(Date()).replace("/", "-")}.csv")
                                FileOutputStream(file).use { it.write(csv.toByteArray()) }
                                val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }, "Exportar Cierre"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) { Text("Exportar Reporte a Excel 📊") }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val exito = viewModel.finalizarCierre(justificacionDescuadre)
                                    if (exito) onRegresar()
                                }
                            },
                            enabled = montoRealInput.isNotBlank() && (abs(diferencia) < 0.01 || justificacionDescuadre.isNotBlank()),
                            modifier = Modifier.fillMaxWidth().height(52.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                        ) { Text("Cerrar Turno y Subir ☁️", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
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
                    minLines = 3
                )
            },
            confirmButton = { Button(onClick = { mostrarDialogoJustificacion = false }) { Text("Guardar") } }
        )
    }
}

@Composable
fun FilaResumenCaja(concepto: String, monto: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = concepto, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(text = String.format("S/. %.2f", monto), fontWeight = FontWeight.SemiBold, color = Color(0xFF1E233D))
    }
}
