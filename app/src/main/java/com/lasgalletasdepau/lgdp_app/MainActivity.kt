package com.lasgalletasdepau.lgdp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
                    // Llamamos a tu contenedor principal para que controle las pantallas
                    AdminContainerScreen()
                }
            }
        }
    }
}