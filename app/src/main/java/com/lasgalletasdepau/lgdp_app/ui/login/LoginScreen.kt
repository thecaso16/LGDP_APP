package com.lasgalletasdepau.lgdp_app.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lasgalletasdepau.lgdp_app.R
import com.lasgalletasdepau.lgdp_app.ui.login.LoginState
import com.lasgalletasdepau.lgdp_app.ui.login.LoginViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val resetState by viewModel.resetPasswordState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showResetDialog by remember { mutableStateOf(false) }

    // Observar el estado de login
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("¡Inicio de sesión exitoso!")
                }
                onLoginSuccess((loginState as LoginState.Success).rol)
            }
            is LoginState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((loginState as LoginState.Error).mensaje)
                }
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. FONDO DE COLOR (FALLLBACK SI LA IMAGEN FALLA)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E233D))
            )

            // 2. IMAGEN DE FONDO
            Image(
                painter = painterResource(id = R.drawable.bg_cafe),
                contentDescription = "Fondo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    Color.Black.copy(alpha = 0.6f),
                    androidx.compose.ui.graphics.BlendMode.Darken
                )
            )

            // 3. TARJETA BLANCA FLOTANTE
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
                        text = "BIENVENIDO",
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

                    // Campo: Correo
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { 
                            // No permitir espacios ni saltos de línea en el correo
                            usuario = it.replace("\\s".toRegex(), "") 
                        },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = loginState !is LoginState.Loading,
                        isError = loginState is LoginState.Error,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E233D),
                            unfocusedBorderColor = Color.LightGray,
                            errorBorderColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo: Contraseña
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { 
                            // No permitir espacios ni saltos de línea en la contraseña
                            contrasena = it.replace("\\s".toRegex(), "") 
                        },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        enabled = loginState !is LoginState.Loading,
                        isError = loginState is LoginState.Error,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1E233D),
                            unfocusedBorderColor = Color.LightGray,
                            errorBorderColor = Color.Red
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.clickable { showResetDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón
                    Button(
                        onClick = { viewModel.iniciarSesion(usuario, contrasena) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                        shape = RoundedCornerShape(24.dp),
                        enabled = loginState !is LoginState.Loading
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
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

    if (showResetDialog) {
        ForgotPasswordDialog(
            state = resetState,
            onDismiss = {
                showResetDialog = false
                viewModel.resetearEstadoPassword()
            },
            onConfirm = { email ->
                viewModel.enviarCorreoRecuperacion(email)
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    state: ResetPasswordState,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (state !is ResetPasswordState.Success) {
                Button(
                    onClick = { onConfirm(email) },
                    enabled = state !is ResetPasswordState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D))
                ) {
                    if (state is ResetPasswordState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar")
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        title = {
            Text(
                text = if (state is ResetPasswordState.Success) "¡Enviado!" else "Recuperar Contraseña",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state is ResetPasswordState.Success) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Hemos enviado un enlace de recuperación a tu correo electrónico.")
                } else {
                    Text("Ingresa tu correo para enviarte un enlace de recuperación.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        isError = state is ResetPasswordState.Error
                    )
                    if (state is ResetPasswordState.Error) {
                        Text(
                            text = state.mensaje,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {})
}