package com.lasgalletasdepau.lgdp_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuadreCajaScreen(onRegresar: () -> Unit) { // <-- Nombre corregido para coincidir con el archivo
    // --- DATOS DEL SISTEMA ---
    val efectivoSistema = 420.50
    val yapeSistema = 350.00
    val izipaySistema = 280.00
    val totalSistema = efectivoSistema + yapeSistema + izipaySistema // S/. 1,050.50

    // --- ESTADOS ---
    var montoRealInput by remember { mutableStateOf("") }
    var justificacionDescuadre by remember { mutableStateOf("") }
    var mostrarDialogoJustificacion by remember { mutableStateOf(false) }

    val montoRealDouble = montoRealInput.toDoubleOrNull() ?: 0.0
    val diferencia = montoRealDouble - totalSistema

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cierre de Caja", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D)) // Nuestro Navy institucional
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC)) // Fondo gris azulado ultra limpio
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- INFO DE ENCABEZADO ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E233D).copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Operador: Carlos Segura",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E233D)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Fecha: 20/06/2026", fontSize = 13.sp, color = Color.Gray)
                            Text(text = "Hora Cierre: 10:15 PM", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- TARJETA 1: REPORTE DEL SISTEMA ---
            item {
                Text(text = "1. Ventas registradas en sistema", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilaDineroModerna(metodo = "Efectivo", monto = efectivoSistema)
                        FilaDineroModerna(metodo = "Yape / Plin", monto = yapeSistema)
                        FilaDineroModerna(metodo = "Izipay", monto = izipaySistema)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Total Esperado", fontWeight = FontWeight.Black, color = Color(0xFF1E233D), fontSize = 16.sp)
                            Text(text = String.format("S/. %.2f", totalSistema), fontWeight = FontWeight.Black, color = Color(0xFF1E233D), fontSize = 16.sp)
                        }
                    }
                }
            }

            // --- TARJETA 2: MONTO REAL (INPUT) ---
            item {
                Text(text = "2. Verificación de efectivo físico", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "¿Cuánto dinero hay en caja realmente?", fontSize = 14.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = montoRealInput,
                            onValueChange = { montoRealInput = it },
                            placeholder = { Text("S/. 0.00", fontSize = 18.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1E233D),
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            singleLine = true
                        )

                        // Justificación interactiva elegante
                        if (diferencia != 0.0 && montoRealInput.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = Color(0xFF3B82F6).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoJustificacion = true }
                            ) {
                                Text(
                                    text = if (justificacionDescuadre.isBlank()) "⚠️ Hay un descuadre. Agregar una justificación aquí." else "📝 Justificación añadida (Toca para editar)",
                                    color = Color(0xFF1D4ED8),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- TARJETA 3: ESTADO DEL BALANCE ---
            item {
                data class EstadoUI(
                    val colorFondo: Color,
                    val colorTexto: Color,
                    val icono: androidx.compose.ui.graphics.vector.ImageVector,
                    val estadoTitulo: String,
                    val mensaje: String
                )

                val estado = when {
                    montoRealInput.isBlank() -> EstadoUI(
                        colorFondo = Color(0xFFF1F5F9),
                        colorTexto = Color(0xFF475569),
                        icono = Icons.Default.Info,
                        estadoTitulo = "Esperando Monto",
                        mensaje = "Por favor introduce el dinero contado en físico."
                    )
                    diferencia == 0.0 -> EstadoUI(
                        colorFondo = Color(0xFFDCFCE7),
                        colorTexto = Color(0xFF15803D),
                        icono = Icons.Default.CheckCircle,
                        estadoTitulo = "Balance Cuadrado",
                        mensaje = "¡Perfecto! El dinero físico coincide con el sistema."
                    )
                    diferencia < 0.0 -> EstadoUI(
                        colorFondo = Color(0xFFFEE2E2),
                        colorTexto = Color(0xFFB91C1C),
                        icono = Icons.Default.Error,
                        estadoTitulo = "Balance Descuadrado (Faltante)",
                        mensaje = String.format("Faltan S/. %.2f en caja.", abs(diferencia))
                    )
                    else -> EstadoUI(
                        colorFondo = Color(0xFFDBEAFE),
                        colorTexto = Color(0xFF1D4ED8),
                        icono = Icons.Default.Info,
                        estadoTitulo = "Balance con Sobrante",
                        mensaje = String.format("Sobrante de S/. %.2f detectado.", diferencia)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = estado.colorFondo),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = estado.icono,
                            contentDescription = null,
                            tint = estado.colorTexto,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = estado.estadoTitulo,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = estado.colorTexto
                            )
                            Text(
                                text = estado.mensaje,
                                fontSize = 13.sp,
                                color = estado.colorTexto.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // --- BOTÓN ACCIÓN FINAL ---
            item {
                Button(
                    onClick = { /* Acción */ },
                    enabled = montoRealInput.isNotBlank() && (diferencia == 0.0 || justificacionDescuadre.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E233D),
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(text = "Finalizar y Cerrar Turno", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }

    // --- DIÁLOGO DE JUSTIFICACIÓN ---
    if (mostrarDialogoJustificacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoJustificacion = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Justificar Descuadre", fontWeight = FontWeight.Black, color = Color(0xFF1E233D)) },
            text = {
                OutlinedTextField(
                    value = justificacionDescuadre,
                    onValueChange = { justificacionDescuadre = it },
                    placeholder = { Text("Ej. El cliente de la mesa 3 se retiró debiendo S/. 5.00...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogoJustificacion = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) { Text("Guardar Nota") }
            }
        )
    }
}

@Composable
fun FilaDineroModerna(metodo: String, monto: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = metodo, color = Color(0xFF64748B), fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Text(text = String.format("S/. %.2f", monto), color = Color(0xFF1E233D), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun CuadreCajaScreenPreview() { // <-- Nombre de la vista previa corregido
    CuadreCajaScreen(onRegresar = {})
}