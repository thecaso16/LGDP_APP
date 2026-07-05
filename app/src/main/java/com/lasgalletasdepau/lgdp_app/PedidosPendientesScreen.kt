package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosPendientesScreen(
    onVerDetalle: (String) -> Unit,
    onRegresar: () -> Unit,
    viewModel: PedidoViewModel = viewModel()
) {
    val pedidosActivos by viewModel.pedidosActivos.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    val timeFormat = remember(locale) { SimpleDateFormat("hh:mm a", locale) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comandas Pendientes ⏳", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()) // Solo aplicamos el padding superior aquí
                .background(Color(0xFFF8FAFC))
        ) {
            if (pedidosActivos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos activos", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = pedidosActivos,
                        key = { it.pedido.pedidoId } // Clave estable para mejorar el scroll
                    ) { item ->
                        val p = item.pedido
                        val hora = if (p.fecha != null) timeFormat.format(Date(p.fecha)) else "--"
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVerDetalle(p.pedidoId) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Pedido #${p.numeroPedido}", fontWeight = FontWeight.Bold, color = Color(0xFF1E233D))
                                    Text(text = "$hora • Mesa: ${if(p.mesaId != null) "Mesa ${p.mesaId}" else "Para Llevar"}", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Listar productos
                                    Text(
                                        text = item.detalles.joinToString { "${it.cantidad}x ${it.nombreProducto}" },
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 2
                                    )
                                }

                                Text(
                                    text = String.format("S/ %.2f", p.total),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E233D),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
