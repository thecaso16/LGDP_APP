package com.lasgalletasdepau.lgdp_app.ui.mesas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableBar
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
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.ui.mesas.SalonViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalonScreen(
    onIrAPedido: (Int, String) -> Unit,
    onIrADetalleMesa: (Int) -> Unit,
    onIrAPedidoParaLlevar: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: SalonViewModel = viewModel()
) {
    val mesas by viewModel.mesas.collectAsState()
    val cajaAbierta by viewModel.cajaAbierta.collectAsState()

    var mostrarDialogoApertura by remember { mutableStateOf(false) }
    var mostrarDialogoLlevar by remember { mutableStateOf(false) }
    var mesaSeleccionadaParaAbrir by remember { mutableStateOf<MesaEntity?>(null) }
    var nombreClienteInput by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Salón de Mesas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E233D),
                modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
            )
            Text(
                text = "Seleccione una mesa para iniciar el pedido:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (cajaAbierta == null) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "La caja está cerrada. Solo puede visualizar el estado de las mesas.",
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(mesas) { mesa ->
                    ItemMesa(
                        mesa = mesa,
                        onClickMesa = {
                            if (mesa.estado == EstadoMesa.OCUPADA) {
                                onIrADetalleMesa(mesa.id)
                            } else {
                                if (cajaAbierta == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Debe abrir caja para iniciar una atención.") }
                                } else {
                                    mesaSeleccionadaParaAbrir = mesa
                                    nombreClienteInput = ""
                                    mostrarDialogoApertura = true
                                }
                            }
                        }
                    )
                }
            }
        }

        // FAB: Para llevar
        ExtendedFloatingActionButton(
            onClick = {
                if (cajaAbierta == null) {
                    scope.launch { snackbarHostState.showSnackbar("Debe abrir caja antes de registrar pedidos.") }
                } else {
                    nombreClienteInput = ""
                    mostrarDialogoLlevar = true
                }
            },
            containerColor = Color(0xFF10B981),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.LocalMall, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Para llevar", fontWeight = FontWeight.Bold)
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    // Diálogos ...
    if (mostrarDialogoApertura && mesaSeleccionadaParaAbrir != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoApertura = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                val mesaNum = mesaSeleccionadaParaAbrir?.numero ?: ""
                val tituloMesa = if (mesaNum.contains("Mesa", ignoreCase = true)) mesaNum else "Mesa $mesaNum"
                Text(
                    text = "Abrir $tituloMesa",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E233D)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ingrese el nombre del cliente:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = nombreClienteInput,
                        onValueChange = { nombreClienteInput = it },
                        placeholder = { Text("Ej. Juan Pérez") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    enabled = nombreClienteInput.isNotBlank()
                ) {
                    Text("Iniciar Atención", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoApertura = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (mostrarDialogoLlevar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLlevar = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Pedido para llevar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E233D)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Ingrese el nombre del cliente:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF475569)
                    )
                    OutlinedTextField(
                        value = nombreClienteInput,
                        onValueChange = { nombreClienteInput = it },
                        placeholder = { Text("Nombre del cliente") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarDialogoLlevar = false
                        onIrAPedidoParaLlevar(nombreClienteInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    enabled = nombreClienteInput.isNotBlank()
                ) {
                    Text("Tomar Pedido", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLlevar = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemMesa(mesa: MesaEntity, onClickMesa: () -> Unit) {
    val estaOcupada = mesa.estado == EstadoMesa.OCUPADA
    val nombreMesa = if (mesa.numero.contains("Mesa", ignoreCase = true)) mesa.numero else "Mesa ${mesa.numero}"
    
    val containerColor = if (estaOcupada) Color(0xFFFEE2E2) else Color.White
    val contentColor = if (estaOcupada) Color(0xFF991B1B) else Color(0xFF1E233D)
    val borderColor = if (estaOcupada) Color(0xFFF87171) else Color(0xFFE2E8F0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .combinedClickable(onClick = { onClickMesa() }),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TableBar,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
                Surface(
                    color = if (estaOcupada) Color(0xFFEF4444) else Color(0xFF10B981),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (estaOcupada) "Ocupada" else "Libre",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = nombreMesa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (estaOcupada) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = contentColor.copy(alpha = 0.6f))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = mesa.clienteActivo ?: "Ocupada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = contentColor.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "Disponible",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
