package com.lasgalletasdepau.lgdp_app.ui.login

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToAdmin: () -> Unit,
    onNavigateToTrabajador: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val context = LocalContext.current
    // Escuchamos de forma reactiva el estado del LoginViewModel
    val state by viewModel.loginState.collectAsState()

    // Manejo de eventos / efectos cuando cambia el estado del Login
    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> {
                val rol = (state as LoginState.Success).rol
                Toast.makeText(context, "¡Ingreso exitoso! Rol: $rol", Toast.LENGTH_SHORT).show()

                // Enrutamiento según el rol guardado en Firestore
                if (rol.equals("Administrador", ignoreCase = true)) {
                    onNavigateToAdmin()
                } else {
                    onNavigateToTrabajador()
                }
                viewModel.resetearEstado()
            }
            is LoginState.Error -> {
                val mensajeError = (state as LoginState.Error).mensaje
                Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                viewModel.resetearEstado()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. IMAGEN DE FONDO
        Image(
            painter = painterResource(id = R.drawable.bg_cafe),
            contentDescription = "Fondo Cafetería",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f), BlendMode.Darken)
        )

        // 2. TARJETA BLANCA FLOTANTE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BIENVENIDO 🍪",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Inicia sesión en Las Galletas de Pau",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo: Correo Electrónico (Firebase Auth requiere formato email)
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state !is LoginState.Loading, // Deshabilitar si está cargando
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E293B),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: Contraseña
                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { contrasena = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state !is LoginState.Loading, // Deshabilitar si está cargando
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E293B),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Muestra el indicador de progreso o el botón normal
                if (state is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFF1E233D)
                    )
                } else {
                    Button(
                        onClick = { viewModel.iniciarSesion(correo, contrasena) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Iniciar Sesión",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToAdmin = {}, onNavigateToTrabajador = {})
}