package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.ui.mesas.DetalleMesaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleMesaScreen(
    mesaId: Int? = null,
    pedidoId: String? = null,
    onIrAPedidoEdicion: (Int?, String?) -> Unit,
    onRegresarAlSalon: () -> Unit,
    onIrAHistorial: () -> Unit,
    viewModel: DetalleMesaViewModel = viewModel()
) {
    val pedido by viewModel.pedido.collectAsState()
    val detalles by viewModel.detalles.collectAsState()

    var mostrarMetodosPago by remember { mutableStateOf(false) }
    var mostrarConfirmarCancelacion by remember { mutableStateOf(false) }

    LaunchedEffect(mesaId, pedidoId) {
        viewModel.cargarDatosMesa(mesaId, pedidoId)
    }

    val nombreCliente = pedido?.nombreCliente ?: "Cargando..."
    val titulo = if (mesaId != null) "Mesa ${mesaId.toString().padStart(2, '0')}" else "Para Llevar"
    val totalConsumido = pedido?.total ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de Cuenta", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onRegresarAlSalon) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8FAFC)).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = titulo, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                            Surface(color = Color(0xFFEF4444).copy(alpha = 0.1f), contentColor = Color(0xFFEF4444), shape = RoundedCornerShape(8.dp)) {
                                Text(text = "Cuenta Activa", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Cliente: $nombreCliente", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Consumo Detallado 🍪", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
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
                                Text(text = "${item.cantidad} x ${item.nombreProducto}", fontSize = 14.sp, color = Color(0xFF334155))
                                Text(text = String.format("S/. %.2f", item.cantidad * item.precioUnitario), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "TOTAL A PAGAR:", fontWeight = FontWeight.Black, color = Color.Gray)
                            Text(text = String.format("S/. %.2f", totalConsumido), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { mostrarMetodosPago = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Registrar Pago y Finalizar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onIrAPedidoEdicion(pedido?.mesaId, pedido?.pedidoId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF1E233D))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF1E233D))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modificar Productos", color = Color(0xFF1E233D), fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { mostrarConfirmarCancelacion = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("❌ Cancelar Toda la Comanda", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (mostrarMetodosPago) {
        AlertDialog(
            onDismissRequest = { mostrarMetodosPago = false },
            title = { Text("Seleccione Método de Pago") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetodoPago.entries.forEach { metodo ->
                        Button(
                            onClick = {
                                viewModel.pagarPedido(metodo) {
                                    mostrarMetodosPago = false
                                    onIrAHistorial()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black)
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
            title = { Text("¿Cancelar Pedido?") },
            text = { Text("Se liberará la mesa y se anularán los consumos.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.cancelarPedido {
                        mostrarConfirmarCancelacion = false
                        onRegresarAlSalon()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                    Text("Confirmar Anulación")
                }
            },
            dismissButton = { TextButton(onClick = { mostrarConfirmarCancelacion = false }) { Text("Volver") } }
        )
    }
}
