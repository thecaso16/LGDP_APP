package com.lasgalletasdepau.lgdp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.ui.login.LoginScreen
import com.lasgalletasdepau.lgdp_app.ui.login.LoginViewModel
import com.lasgalletasdepau.lgdp_app.ui.theme.LGDP_APPTheme
import com.lasgalletasdepau.lgdp_app.utils.NetworkMonitor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos los componentes de sincronización
        val syncManager = SyncManager.getInstance(this)
        val networkMonitor = NetworkMonitor(this)

        // Lanzamos la observación de red ligada al ciclo de vida de la Activity
        lifecycleScope.launch {
            networkMonitor.isConnected.collectLatest { estaConectado ->
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (estaConectado && currentUser != null) {
                    // Sincroniza automáticamente al detectar internet solo si hay sesión
                    syncManager.sincronizarTodo()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            LGDP_APPTheme {
                val viewModel: LoginViewModel = viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var userRole by remember { mutableStateOf<String?>(null) }
                    var isLoggedIn by remember { mutableStateOf(false) }

                    if (!isLoggedIn) {
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { role ->
                                userRole = role
                                isLoggedIn = true
                                // Disparar sincronización al iniciar sesión exitosamente
                                lifecycleScope.launch {
                                    syncManager.sincronizarTodo()
                                }
                            }
                        )
                    } else {
                        // Navegación según el rol
                        if (userRole == "Administrador") {
                            AdminContainerScreen(
                                onLogout = {
                                    viewModel.cerrarSesion()
                                    isLoggedIn = false
                                    userRole = null
                                }
                            )
                        } else {
                            MainContainerScreen(
                                onLogout = {
                                    viewModel.cerrarSesion()
                                    isLoggedIn = false
                                    userRole = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}