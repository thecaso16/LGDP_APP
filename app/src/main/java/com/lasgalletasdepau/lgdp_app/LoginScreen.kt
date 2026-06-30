package com.lasgalletasdepau.lgdp_app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun LoginScreen() {
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Un Box permite superponer elementos (Tarjeta encima de la Imagen)
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. IMAGEN DE FONDO (El ambiente de la cafetería)
        // NOTA: Para que funcione, guarda la foto de la cafetería en res/drawable con el nombre "bg_cafe"
        Image(
            painter = painterResource(id = R.drawable.bg_cafe),
            contentDescription = "Fondo Cafetería",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            // Aplicamos un tinte oscuro idéntico al de la imagen de referencia para dar contraste
            colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.5f), androidx.compose.ui.graphics.BlendMode.Darken)
        )

        // 2. TARJETA BLANCA FLOTANTE (Centrada en la pantalla)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(32.dp), // Bordes bien curvados como en tu imagen
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título Principal con el emoji de galleta 🍪
                Text(
                    text = "BIENVENIDO 🍪",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A1A) // Negro suave
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtítulo
                Text(
                    text = "Inicia sesión en Las Galletas de Pau",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Campo: Nombre de Usuario
                OutlinedTextField(
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E293B),
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Botón Azul Oscuro / Navy
                Button(
                    onClick = { /* Lógica de Firebase Auth */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    // Color institucional extraído de image_718c43.png (#1E233D aprox)
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E233D)),
                    shape = RoundedCornerShape(24.dp) // Totalmente redondeado
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}