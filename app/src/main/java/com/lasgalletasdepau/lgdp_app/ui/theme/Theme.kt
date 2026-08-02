package com.lasgalletasdepau.lgdp_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBFDBFE), // Azul más claro para mayor visibilidad
    onPrimary = NavyBrand,
    secondary = Color(0xFF34D399), // Verde esmeralda más claro
    onSecondary = NavyBrand,
    background = Color(0xFF0F172A), 
    surface = Color(0xFF1E293B),    
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155), 
    onSurfaceVariant = Color(0xFFE2E8F0), 
    outlineVariant = Color(0xFF475569)   
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBrand,
    onPrimary = Color.White,
    secondary = EmeraldBrand,
    onSecondary = Color.White,
    background = LightBackground,
    surface = Color.White,
    onBackground = NavyBrand,
    onSurface = NavyBrand,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B) // Gris más oscuro para mejor contraste
)

@Composable
fun LGDP_APPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para priorizar los colores de marca
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
