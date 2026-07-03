package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

// MODELO PARA EL DETALLE DE PRODUCTOS DE UN PEDIDO
data class ProductoVendido(
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
)

// MODELO DE DATOS PARA LAS VENTAS
data class VentaResumen(
    val id: String,
    val fecha: String, // Formato "dd/MM/yyyy"
    val hora: String,
    val total: Double,
    val metodoPago: String,
    val atendidoPor: String,
    val productos: List<ProductoVendido>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesTrabajadoresScreen() {
    // 1. ESTADOS PARA LOS FILTROS DE FECHA
    var fechaInicio by remember { mutableStateOf("01/07/2026") }
    var fechaFin by remember { mutableStateOf("03/07/2026") }

    // ESTADO PARA MOSTRAR EL DETALLE DEL PEDIDO SELECCIONADO
    var pedidoSeleccionado by remember { mutableStateOf<VentaResumen?>(null) }

    // 2. BASE DE DATOS DE PRUEBA
    val listadoTransacciones = remember {
        listOf(
            VentaResumen(
                id = "#1024", fecha = "03/07/2026", hora = "08:30 AM", total = 45.50, metodoPago = "Yape", atendidoPor = "María Delgado",
                productos = listOf(
                    ProductoVendido("Capuccino Grande", 2, 12.00),
                    ProductoVendido("Galleta Choco-Chip XL", 3, 5.50),
                    ProductoVendido("Sandwich de Pollo", 1, 5.00)
                )
            ),
            VentaResumen(
                id = "#1025", fecha = "03/07/2026", hora = "09:15 AM", total = 12.00, metodoPago = "Efectivo", atendidoPor = "Carlos Ruiz",
                productos = listOf(
                    ProductoVendido("Americano Mediano", 1, 7.00),
                    ProductoVendido("Alfajor Artesanal", 2, 2.50)
                )
            ),
            VentaResumen(
                id = "#1026", fecha = "02/07/2026", hora = "10:02 AM", total = 28.00, metodoPago = "Plin", atendidoPor = "María Delgado",
                productos = listOf(
                    ProductoVendido("Frappé de Oreo", 2, 14.00)
                )
            ),
            VentaResumen(
                id = "#1027", fecha = "01/07/2026", hora = "11:45 AM", total = 65.00, metodoPago = "Tarjeta", atendidoPor = "Carlos Ruiz",
                productos = listOf(
                    ProductoVendido("Torta de Chocolate (Porción)", 3, 15.00),
                    ProductoVendido("Latte Helado", 2, 10.00)
                )
            )
        )
    }

    // 3. LÓGICA DE FILTRADO REAL EN TIEMPO REAL
    val transaccionesFiltradas = remember(fechaInicio, fechaFin) {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val dateInicio = formato.parse(fechaInicio)
            val dateFin = formato.parse(fechaFin)

            if (dateInicio != null && dateFin != null) {
                listadoTransacciones.filter { venta ->
                    val dateVenta = formato.parse(venta.fecha)
                    // Verifica si la fecha de la venta está dentro del rango (inclusive)
                    dateVenta != null && !dateVenta.before(dateInicio) && !dateVenta.after(dateFin)
                }
            } else {
                listadoTransacciones
            }
        } catch (e: Exception) {
            // Si el usuario escribe algo inválido mientras edita, muestra todo para evitar que se rompa la app
            listadoTransacciones
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Transacciones 📋", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {

            // SECCIÓN: FILTRADO POR RANGO DE FECHAS
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
                        // Fecha Inicio
                        OutlinedTextField(
                            value = fechaInicio,
                            onValueChange = { fechaInicio = it },
                            label = { Text("Desde (dd/mm/aaaa)") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Fecha Fin
                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { fechaFin = it },
                            label = { Text("Hasta (dd/mm/aaaa)") },
                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // SECCIÓN: LISTADO DE TRANSACCIONES FILTRADAS
            Text(
                text = "Movimientos Encontrados (${transaccionesFiltradas.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF1E233D),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (transaccionesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay transacciones en este rango de fechas.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transaccionesFiltradas) { venta ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pedidoSeleccionado = venta }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Pedido ${venta.id}", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                                    Text(text = "${venta.fecha} • ${venta.hora}", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "Por: ${venta.atendidoPor}", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "S/ ${String.format("%.2f", venta.total)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E233D)
                                    )
                                    Text(
                                        text = venta.metodoPago,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE DETALLE COMPLETO DEL PEDIDO ---
    if (pedidoSeleccionado != null) {
        val pedido = pedidoSeleccionado!!
        AlertDialog(
            onDismissRequest = { pedidoSeleccionado = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Column {
                    Text(text = "Detalle del Pedido ${pedido.id}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E233D))
                    Text(text = "${pedido.fecha} a las ${pedido.hora}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                    Text(text = "Atendido por: ${pedido.atendidoPor}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))
                    Text(text = "Método de Pago: ${pedido.metodoPago}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF475569))

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Productos:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E233D))

                    pedido.productos.forEach { prod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${prod.cantidad}x ${prod.nombre}", fontSize = 13.sp, color = Color(0xFF1E233D))
                            Text(text = "S/ ${String.format("%.2f", prod.precioUnitario * prod.cantidad)}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                    Text("Cerrar Detalle")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportesTrabajadoresScreenPreview() {
    ReportesTrabajadoresScreen()
}