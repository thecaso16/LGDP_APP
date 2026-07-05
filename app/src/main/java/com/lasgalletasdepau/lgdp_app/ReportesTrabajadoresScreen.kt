package com.lasgalletasdepau.lgdp_app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PointOfSale
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoConDetalles
import com.lasgalletasdepau.lgdp_app.ui.pedidos.ReportesTrabajadoresViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesTrabajadoresScreen(
    onIrACierreCaja: () -> Unit,
    viewModel: ReportesTrabajadoresViewModel = viewModel()
) {
    val context = LocalContext.current
    val historial by viewModel.historial.collectAsState()
    val usuario by viewModel.usuarioLogueado.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val sdf = remember(locale) { SimpleDateFormat("dd/MM/yyyy", locale) }
    val hoy = remember(sdf) { sdf.format(Date()) }

    var fechaInicio by remember { mutableStateOf(hoy) }
    var fechaFin by remember { mutableStateOf(hoy) }

    var pedidoSeleccionado by remember { mutableStateOf<PedidoConDetalles?>(null) }

    // Cargar datos iniciales (pedidos de hoy)
    LaunchedEffect(usuario) {
        if (usuario != null) {
            viewModel.buscarPorRango(fechaInicio, fechaFin)
        }
    }

    // Actualizar cada vez que se pulsa el botón de buscar
    val onBuscar = {
        viewModel.buscarPorRango(fechaInicio, fechaFin)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Transacciones 📋", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D)),
                actions = {
                    IconButton(onClick = onIrACierreCaja) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = "Cierre de Caja",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (historial.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val csvData = viewModel.generarCsvData()
                        val file = File(context.cacheDir, "Reporte_Ventas_${fechaInicio.replace("/", "-")}.csv")
                        try {
                            FileOutputStream(file).use { it.write(csvData.toByteArray()) }
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Exportar Reporte Excel (CSV)"))
                        } catch (e: Exception) {
                            // Error al exportar
                        }
                    },
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Exportar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Reporte 📊", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rango de Búsqueda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1E233D)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fechaInicio,
                            onValueChange = { fechaInicio = it },
                            label = { Text("Desde") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { fechaFin = it },
                            label = { Text("Hasta") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    
                    Button(
                        onClick = onBuscar,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Buscar Historial")
                    }
                }
            }

            Text(
                text = "Movimientos Encontrados (${historial.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1E233D),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (historial.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay transacciones registradas.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historial) { item ->
                        val pedido = item.pedido
                        val timeSdf = remember(locale) { SimpleDateFormat("hh:mm a", locale) }
                        val hora = if (pedido.fecha != null) timeSdf.format(Date(pedido.fecha)) else "--:--"
                        val fechaStr = if (pedido.fecha != null) sdf.format(Date(pedido.fecha)) else ""

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth().clickable { pedidoSeleccionado = item }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Pedido #${pedido.numeroPedido}", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                                    Text(text = "$fechaStr • $hora", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "Mesa: ${pedido.mesaId ?: "Llevar"}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "S/ ${String.format("%.2f", pedido.total)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E233D)
                                    )
                                    Text(
                                        text = pedido.metodoPago?.valor ?: "Pendiente",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (pedido.metodoPago != null) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pedidoSeleccionado != null) {
        val item = pedidoSeleccionado!!
        val pedido = item.pedido
        val detalles = item.detalles
        
        AlertDialog(
            onDismissRequest = { pedidoSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Column {
                    Text(text = "Detalle del Pedido #${pedido.numeroPedido}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E233D))
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    Text(text = "Método de Pago: ${pedido.metodoPago?.valor ?: "No definido"}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                    Text(text = "Cliente: ${pedido.nombreCliente ?: "General"}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Productos:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E233D))

                    detalles.forEach { prod ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${prod.cantidad}x ${prod.nombreProducto}", fontSize = 13.sp, color = Color(0xFF1E233D))
                            Text(text = "S/ ${String.format("%.2f", prod.precioUnitario * prod.cantidad)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Neto:", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E233D))
                        Text(text = "S/ ${String.format("%.2f", pedido.total)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E233D))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { pedidoSeleccionado = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}
