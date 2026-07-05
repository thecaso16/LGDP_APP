package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.ui.mesas.SalonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonScreen(
    onIrAPedido: (Int, String) -> Unit,
    onIrADetalleMesa: (Int) -> Unit,
    onIrAPedidoParaLlevar: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SalonViewModel = viewModel()
) {
    val mesas by viewModel.mesas.collectAsState()

    var mostrarDialogoApertura by remember { mutableStateOf(false) }
    var mesaSeleccionadaParaAbrir by remember { mutableStateOf<MesaEntity?>(null) }
    var nombreClienteInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Salón de Mesas 🪑", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E233D)),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onIrAPedidoParaLlevar,
                containerColor = Color(0xFF10B981),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalMall,
                    contentDescription = "Pedido para Llevar"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Para Llevar 🛍️", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(16.dp)
        ) {
            Text(
                text = "Seleccione una mesa o use el botón inferior para llevar:",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mesas) { mesa ->
                    ItemMesa(
                        mesa = mesa,
                        onClickMesa = {
                            if (mesa.estado == EstadoMesa.OCUPADA) {
                                onIrADetalleMesa(mesa.id)
                            } else {
                                mesaSeleccionadaParaAbrir = mesa
                                nombreClienteInput = ""
                                mostrarDialogoApertura = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (mostrarDialogoApertura && mesaSeleccionadaParaAbrir != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoApertura = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Abrir Mesa ${mesaSeleccionadaParaAbrir?.numero} 📝",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E233D)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ingrese el nombre del cliente para identificar la comanda:",
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = nombreClienteInput,
                        onValueChange = { nombreClienteInput = it },
                        placeholder = { Text("Ej. Juan Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mesaId = mesaSeleccionadaParaAbrir!!.id
                        viewModel.abrirMesa(mesaId, nombreClienteInput)
                        mostrarDialogoApertura = false
                        onIrAPedido(mesaId, nombreClienteInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Iniciar Atención", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarDialogoApertura = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemMesa(mesa: MesaEntity, onClickMesa: () -> Unit) {
    val estaOcupada = mesa.estado == EstadoMesa.OCUPADA
    val fondoColor = if (estaOcupada) Color(0xFFEF4444).copy(alpha = 0.08f) else Color.White
    val bordeColor = if (estaOcupada) Color(0xFFEF4444) else Color(0xFFE2E8F0)
    val tituloColor = Color(0xFF1E233D)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .combinedClickable(
                onClick = { onClickMesa() }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = fondoColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, bordeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mesa.numero}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = tituloColor
                )
                Surface(
                    color = if (estaOcupada) Color(0xFFEF4444) else Color(0xFF10B981),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (estaOcupada) "Ocupada" else "Libre",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (estaOcupada) {
                Text(
                    text = "👤 ${mesa.clienteActivo ?: "Cliente"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569)
                )
            } else {
                Text(
                    text = "Lista para usar",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalonScreenPreview() {
    // Para el preview, como no tenemos ViewModel real, esto podría fallar o mostrarse vacío.
    // Idealmente usaríamos un ViewModel falso o pasaríamos los datos directamente.
}