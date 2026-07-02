package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainContainerScreen() {
    // CONTROL DE NAVEGACIÓN
    // 0: Salón (SalonScreen)
    // 1: Pedido (PedidoScreen)
    // 2: Detalle de Mesa (DetalleMesaScreen) -> Se abre de forma superpuesta
    var pantallaActual by remember { mutableStateOf(0) } // Iniciamos en el Salón por defecto

    var pantallaMenuPrevia by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            // La barra inferior SOLO aparecerá cuando NO estemos en el Salón (pantallas 1 y 2)
            // para ofrecer la salida rápida y limpia hacia el plano de mesas.
            if (pantallaActual != 0) {
                BottomAppBar(
                    containerColor = Color(0xFF1E233D), // Navy institucional
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { pantallaActual = 0 }, // Acción directa de regreso al Salón
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1E233D)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.TableRestaurant, contentDescription = "Regresar al Salón")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Regresar al Salón", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (pantallaActual) {
                0 -> SalonScreen(
                    onIrAPedido = { pantallaActual = 1 },
                    onIrADetalleMesa = { pantallaActual = 2 }
                )
                1 -> PedidoScreen(
                    onBackToSalon = { pantallaActual = 0 }
                )
                2 -> DetalleMesaScreen(
                    onIrAPedidoEdicion = { pantallaActual = 1 },
                    onRegresarAlSalon = { pantallaActual = pantallaMenuPrevia }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContainerScreenPreview() {
    MainContainerScreen()
}