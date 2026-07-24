package com.lasgalletasdepau.lgdp_app.ui.mesas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.ui.mesas.DetalleMesaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(
    mesaId: Int? = null,
    pedidoId: String? = null,
    onIrAPedidoEdicion: (Int?, String?) -> Unit,
    onRegresarAlSalon: () -> Unit,
    onIrAHistorial: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DetalleMesaViewModel = viewModel()
) {
    val pedido by viewModel.pedido.collectAsState()
    val detalles by viewModel.detalles.collectAsState()
    val cajaAbierta by viewModel.cajaAbierta.collectAsState()

    var mostrarMetodosPago by remember { mutableStateOf(false) }
    var mostrarConfirmarCancelacion by remember { mutableStateOf(false) }
    var justificacionCancelacion by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(mesaId, pedidoId) {
        viewModel.cargarDatosMesa(mesaId, pedidoId)
    }

    val nombreCliente = pedido?.nombreCliente ?: "Sin pedido activo"
    val titulo = if (mesaId != null) "Mesa ${mesaId.toString().padStart(2, '0')}" else "Pedido para llevar"
    val totalConsumido = pedido?.total ?: 0.0
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header con botón de regreso
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRegresarAlSalon) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = Color(0xFF1E233D))
                }
                Text(
                    text = "Estado de Cuenta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E233D)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            Surface(
                                color = if (pedido != null) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), 
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (pedido != null) "Cuenta Activa" else "Mesa Bloqueada", 
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                                    style = MaterialTheme.typography.labelSmall, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (pedido != null) Color(0xFF2E7D32) else Color.Red
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Cliente: $nombreCliente", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    }
                }

                if (pedido != null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = "Consumo detallado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            detalles.forEach { item ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "${item.cantidad} x ${item.nombreProducto}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                                    Text(text = String.format("S/. %.2f", item.cantidad * item.precioUnitario), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Text(text = "TOTAL A PAGAR:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Gray)
                                Text(text = String.format("S/. %.2f", totalConsumido), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "No se encontró una comanda activa para esta mesa.\nSi la mesa aparece ocupada por error, use el botón de abajo para liberarla.", 
                            textAlign = TextAlign.Center, 
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (cajaAbierta == null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        "La caja está cerrada. Debe abrir caja para registrar pagos o modificar pedidos.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (pedido != null) {
                    Button(
                        onClick = {
                            if (cajaAbierta == null) {
                                scope.launch { snackbarHostState.showSnackbar("Debe abrir caja para procesar el pago.") }
                            } else {
                                mostrarMetodosPago = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        enabled = cajaAbierta != null
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Pago y Finalizar", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (cajaAbierta == null) {
                                scope.launch { snackbarHostState.showSnackbar("Debe abrir caja para modificar el pedido.") }
                            } else {
                                onIrAPedidoEdicion(pedido?.mesaId, pedido?.pedidoId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF1E233D)),
                        enabled = cajaAbierta != null
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = if (cajaAbierta != null) Color(0xFF1E233D) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Añadir Productos", color = if (cajaAbierta != null) Color(0xFF1E233D) else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(
                    onClick = { mostrarConfirmarCancelacion = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text(if (pedido != null) "Anular Comanda" else "Liberar Mesa Manualmente", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (mostrarMetodosPago) {
        AlertDialog(
            onDismissRequest = { mostrarMetodosPago = false },
            title = { Text("Seleccione el método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    MetodoPago.entries.forEach { metodo ->
                        Button(
                            onClick = {
                                viewModel.pagarPedido(metodo) {
                                    mostrarMetodosPago = false
                                    onIrAHistorial()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF1E233D)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(metodo.valor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (mostrarConfirmarCancelacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmarCancelacion = false },
            title = { Text(if (pedido != null) "Anular Comanda" else "Confirmar Acción") },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (pedido != null) "¿Por qué desea anular este pedido?" else "¿Está seguro que desea proceder? Esta acción liberará la mesa.")
                    if (pedido != null) {
                        OutlinedTextField(
                            value = justificacionCancelacion,
                            onValueChange = { justificacionCancelacion = it },
                            placeholder = { Text("Ej. Error en digitación...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pedido != null && justificacionCancelacion.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Debe ingresar una justificación.") }
                            return@Button
                        }
                        viewModel.cancelarPedido(justificacionCancelacion) {
                            mostrarConfirmarCancelacion = false
                            onRegresarAlSalon()
                        }
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    enabled = pedido == null || justificacionCancelacion.isNotBlank()
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = { 
                TextButton(onClick = { mostrarConfirmarCancelacion = false }) { 
                    Text("Cancelar", color = Color.Gray) 
                } 
            }
        )
    }
}
