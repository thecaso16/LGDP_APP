package com.lasgalletasdepau.lgdp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lasgalletasdepau.lgdp_app.ui.login.LoginScreen
import com.lasgalletasdepau.lgdp_app.ui.theme.LGDP_APPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LGDP_APPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Estado local para saber en qué pantalla estamos
                    // Valores posibles: "LOGIN", "ADMIN", "TRABAJADOR"
                    var pantallaActual by remember { mutableStateOf("LOGIN") }

                    when (pantallaActual) {
                        "LOGIN" -> {
                            LoginScreen(
                                onNavigateToAdmin = { pantallaActual = "ADMIN" },
                                onNavigateToTrabajador = { pantallaActual = "TRABAJADOR" }
                            )
                        }
                        "Administrador" -> {
                            // Reemplaza esto con tu pantalla real del administrador, por ejemplo:
                            // AdminContainerScreen()
                            Text(text = "Pantalla del Administrador (Cargos, Reportes, Cajas)")
                        }
                        "Trabajador" -> {
                            // Reemplaza esto con tu pantalla real del mozo/cajero, por ejemplo:
                            // PedidosScreen() o SalonScreen()
                            Text(text = "Pantalla del Trabajador / Mozo (Comandas y Mesas)")
                        }
                    }
                }
            }
        }
    }
}