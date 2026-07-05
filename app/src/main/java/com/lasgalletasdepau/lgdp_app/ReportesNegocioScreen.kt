package com.lasgalletasdepau.lgdp_app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CalendarMonth
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.PictureAsPdf
//import androidx.compose.material.icons.filled.TrendingUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//// MODELO PARA EL GRÁFICO DE BARRAS
//data class ProductoEstadistica(
//    val nombre: String,
//    val cantidadVendida: Int,
//    val porcentaje: Float // Valor de 0.0f a 1.0f para el ancho de la barra
//)
//
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesNegocioScreen() {}
//    // 1. ESTADOS PARA LOS FILTROS DE FECHA
//    var fechaInicio by remember { mutableStateOf("01/07/2026") }
//    var fechaFin by remember { mutableStateOf("03/07/2026") }
//
//    // 2. BASE DE DATOS DE PRUEBA ORIGINAL
//    val listadoTransacciones = remember {
//        listOf(
//            VentaResumen(
//                id = "#1024", fecha = "03/07/2026", hora = "08:30 AM", total = 45.50, metodoPago = "Yape", atendidoPor = "María Delgado",
//                productos = listOf(
//                    ProductoVendido("Capuccino Grande", 2, 12.00),
//                    ProductoVendido("Galleta Choco-Chip XL", 3, 5.50),
//                    ProductoVendido("Sandwich de Pollo", 1, 5.00)
//                )
//            ),
//            VentaResumen(
//                id = "#1025", fecha = "03/07/2026", hora = "09:15 AM", total = 12.00, metodoPago = "Efectivo", atendidoPor = "Carlos Ruiz",
//                productos = listOf(
//                    ProductoVendido("Americano Mediano", 1, 7.00),
//                    ProductoVendido("Alfajor Artesanal", 2, 2.50)
//                )
//            ),
//            VentaResumen(
//                id = "#1026", fecha = "02/07/2026", hora = "10:02 AM", total = 28.00, metodoPago = "Plin", atendidoPor = "María Delgado",
//                productos = listOf(
//                    ProductoVendido("Frappé de Oreo", 2, 14.00)
//                )
//            ),
//            VentaResumen(
//                id = "#1027", fecha = "01/07/2026", hora = "11:45 AM", total = 65.00, metodoPago = "Tarjeta", atendidoPor = "Carlos Ruiz",
//                productos = listOf(
//                    ProductoVendido("Torta de Chocolate (Porción)", 3, 15.00),
//                    ProductoVendido("Latte Helado", 2, 10.00)
//                )
//            )
//        )
//    }
//
//    // 3. FILTRADO Y CÁLCULOS EN TIEMPO REAL SEGÚN EL RANGO
//    val transaccionesFiltradas = remember(fechaInicio, fechaFin) {
//        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//        try {
//            val dateInicio = formato.parse(fechaInicio)
//            val dateFin = formato.parse(fechaFin)
//
//            if (dateInicio != null && dateFin != null) {
//                listadoTransacciones.filter { venta ->
//                    val dateVenta = formato.parse(venta.fecha)
//                    dateVenta != null && !dateVenta.before(dateInicio) && !dateVenta.after(dateFin)
//                }
//            } else listadoTransacciones
//        } catch (e: Exception) {
//            listadoTransacciones
//        }
//    }
//
//    // Calcular KPIs Dinámicos
//    val totalIngresos = transaccionesFiltradas.sumOf { it.total }
//    val totalPedidos = transaccionesFiltradas.size
//
//    // 4. GENERAR DATOS PARA EL GRÁFICO DE BARRAS (Top Productos)
//    val productosMasVendidos = remember(transaccionesFiltradas) {
//        val conteoProductos = mutableMapOf<String, Int>()
//
//        // Sumar cantidades de productos vendidos en el rango filtrado
//        transaccionesFiltradas.forEach { venta ->
//            venta.productos.forEach { prod ->
//                conteoProductos[prod.nombre] = (conteoProductos[prod.nombre] ?: 0) + prod.cantidad
//            }
//        }
//
//        // Obtener el producto con la cantidad máxima para calcular los porcentajes de barra proporcionales
//        val maxUnidades = conteoProductos.values.maxOrNull() ?: 1
//
//        conteoProductos.entries
//            .map { ProductoEstadistica(it.key, it.value, it.value.toFloat() / maxUnidades) }
//            .sortedByDescending { it.cantidadVendida } // Ordenar de mayor a menor venta
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Reportes del Negocio 📊", fontWeight = FontWeight.ExtraBold, color = Color.White) },
//                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .background(Color(0xFFF8FAFC))
//        ) {
//
//            // SECCIÓN 1: SELECTOR DE FECHAS
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                colors = CardDefaults.cardColors(containerColor = Color.White),
//                elevation = CardDefaults.cardElevation(1.dp)
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = "Filtrar por Rango de Fechas",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 14.sp,
//                        color = Color(0xFF1E233D)
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        OutlinedTextField(
//                            value = fechaInicio,
//                            onValueChange = { fechaInicio = it },
//                            label = { Text("Desde") },
//                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
//                            modifier = Modifier.weight(1f),
//                            shape = RoundedCornerShape(10.dp)
//                        )
//
//                        OutlinedTextField(
//                            value = fechaFin,
//                            onValueChange = { fechaFin = it },
//                            label = { Text("Hasta") },
//                            trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
//                            modifier = Modifier.weight(1f),
//                            shape = RoundedCornerShape(10.dp)
//                        )
//                    }
//                }
//            }
//
//            // SECCIÓN 2: INDICADORES FINANCIEROS (DÍNAMICOS)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Card(
//                    modifier = Modifier.weight(1f),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(2.dp),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF10B981))
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text("Total Ingresos", fontSize = 12.sp, color = Color.Gray)
//                        Text("S/ ${String.format("%.2f", totalIngresos)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
//                    }
//                }
//
//                Card(
//                    modifier = Modifier.weight(1f),
//                    colors = CardDefaults.cardColors(containerColor = Color.White),
//                    elevation = CardDefaults.cardElevation(2.dp),
//                    shape = RoundedCornerShape(16.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF1E233D))
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text("Órdenes Completadas", fontSize = 12.sp, color = Color.Gray)
//                        Text("$totalPedidos Pedidos", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E233D))
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // SECCIÓN 3: EXPORTACIÓN EXCLUSIVA A PDF
//            Button(
//                onClick = { /* Acción para construir PDF */ },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//                    .height(50.dp)
//            ) {
//                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Exportar Reporte a PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp)
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            // SECCIÓN 4: GRÁFICO DE BARRAS DE LOS MÁS VENDIDOS
//            Text(
//                text = "Top Productos Más Vendidos",
//                fontWeight = FontWeight.Bold,
//                fontSize = 15.sp,
//                color = Color(0xFF1E233D),
//                modifier = Modifier.padding(horizontal = 16.dp)
//            )
//
//            if (productosMasVendidos.isEmpty()) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("No hay datos de ventas disponibles.", color = Color.Gray)
//                }
//            } else {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    verticalArrangement = Arrangement.spacedBy(14.dp)
//                ) {
//                    items(productosMasVendidos) { producto ->
//                        Column(modifier = Modifier.fillMaxWidth()) {
//                            // Fila con los textos: Nombre del producto y cantidad
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = producto.nombre,
//                                    fontWeight = FontWeight.SemiBold,
//                                    fontSize = 13.sp,
//                                    color = Color(0xFF334155)
//                                )
//                                Text(
//                                    text = "${producto.cantidadVendida} und.",
//                                    fontWeight = FontWeight.Bold,
//                                    fontSize = 13.sp,
//                                    color = Color(0xFF1E233D)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(4.dp))
//
//                            // Contenedor de la barra (Fondo gris claro)
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(20.dp)
//                                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(6.dp))
//                            ) {
//                                // Barra de Progreso Colorizada Dinámica (Ancho relativo al porcentaje)
//                                Box(
//                                    modifier = Modifier
//                                        .fillMaxWidth(fraction = producto.porcentaje)
//                                        .fillMaxHeight()
//                                        .background(
//                                            color = Color(0xFF3B82F6), // Color Azul Pastel corporativo
//                                            shape = RoundedCornerShape(6.dp)
//                                        )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ReportesNegocioScreenPreview() {
//    ReportesNegocioScreen()
//}