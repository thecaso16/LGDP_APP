package com.lasgalletasdepau.lgdp_app.ui.main

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
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import com.lasgalletasdepau.lgdp_app.ui.login.LoginScreen
import com.lasgalletasdepau.lgdp_app.ui.login.LoginState
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
                    syncManager.sincronizarTodo()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            LGDP_APPTheme {
                val viewModel: LoginViewModel = viewModel()
                val loginState by viewModel.loginState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = loginState) {
                        is LoginState.Success -> {
                            val userRoleString = state.rol
                            val roles = RolUsuario.fromStringList(userRoleString)

                            if (roles.contains(RolUsuario.ADMINISTRADOR)) {
                                AdminContainerScreen(
                                    onLogout = {
                                        viewModel.cerrarSesion()
                                    }
                                )
                            } else {
                                MainContainerScreen(
                                    userRole = userRoleString,
                                    onLogout = {
                                        viewModel.cerrarSesion()
                                    }
                                )
                            }

                            // Sincronizar cuando el login es exitoso
                            LaunchedEffect(state.rol) {
                                syncManager.sincronizarTodo()
                            }
                        }
                        else -> {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { /* Ya no es necesario manejar estado local aquí */ }
                            )
                        }
                    }
                }
            }
        }
    }
}