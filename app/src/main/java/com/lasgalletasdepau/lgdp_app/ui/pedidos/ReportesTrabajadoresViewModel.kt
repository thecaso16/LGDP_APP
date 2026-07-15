package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoDetalleEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class PedidoConDetalles(
    val pedido: PedidoEntity,
    val detalles: List<PedidoDetalleEntity>
)

class ReportesTrabajadoresViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = com.lasgalletasdepau.lgdp_app.data.remote.SyncManager.getInstance(application)

    // Observar al usuario de forma reactiva
    val usuarioLogueado: StateFlow<UsuarioEntity?> = appDao.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _historial = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val historial: StateFlow<List<PedidoConDetalles>> = _historial

    fun esCajeroOAdmin(): Boolean {
        val user = usuarioLogueado.value ?: return false
        val roles = RolUsuario.fromStringList(user.rol)
        return roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
    }

    /**
     * Verifica si el usuario actual es el cajero que abrió la caja actual.
     */
    fun esCajeroResponsable(cajeroIdEnCaja: String?): Boolean {
        val user = usuarioLogueado.value ?: return false
        return user.uid == cajeroIdEnCaja
    }

    fun buscarPorRango(fechaInicioStr: String, fechaFinStr: String) {
        val user = usuarioLogueado.value ?: return
        viewModelScope.launch {
            try {
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val inicioDate = format.parse(fechaInicioStr) ?: Date()
                val finDate = format.parse(fechaFinStr) ?: Date()
                
                val calInicio = Calendar.getInstance().apply {
                    time = inicioDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val calFin = Calendar.getInstance().apply {
                    time = finDate
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                // 1. Mostrar datos locales inmediatamente con el UID correcto
                actualizarHistorialLocal(user.uid, calInicio.timeInMillis, calFin.timeInMillis)

                // 2. Descargar historial de Firebase filtrado por este usuario
                syncManager.bajarHistorialRango(user.uid, calInicio.timeInMillis, calFin.timeInMillis)

                // 3. Refrescar datos locales
                actualizarHistorialLocal(user.uid, calInicio.timeInMillis, calFin.timeInMillis)
            } catch (e: Exception) {
                // Mantener estado actual en caso de error
            }
        }
    }

    private suspend fun actualizarHistorialLocal(uid: String, inicio: Long, fin: Long) {
        // Los trabajadores solo ven sus propios pedidos (verTodo = 0)
        // Los administradores podrían ver todo (verTodo = 1) si se desea, 
        // pero para evitar la mezcla reportada, mantendremos el filtro estricto por ahora.
        val roles = RolUsuario.fromStringList(usuarioLogueado.value?.rol)
        val verTodo = if (roles.contains(RolUsuario.ADMINISTRADOR)) 1 else 0
        
        val pedidos = appDao.obtenerPedidosHistorial(uid, inicio, fin, verTodo)
        val resultado = pedidos.map { pedido ->
            val detalles = appDao.obtenerDetallesPorPedido(pedido.pedidoId)
            PedidoConDetalles(pedido, detalles)
        }
        _historial.value = resultado
    }

    fun generarCsvData(): String {
        val sb = StringBuilder()
        sb.append("Cantidad;Producto;Precio Unitario;Subtotal;Total Pedido;Metodo Pago\n")
        
        _historial.value.forEach { item ->
            val pedido = item.pedido
            val detalles = item.detalles
            
            detalles.forEach { det ->
                sb.append("${det.cantidad};")
                sb.append("${det.nombreProducto};")
                sb.append(String.format("%.2f", det.precioUnitario).replace(".", ",") + ";")
                sb.append(String.format("%.2f", det.cantidad * det.precioUnitario).replace(".", ",") + ";")
                sb.append(String.format("%.2f", pedido.total).replace(".", ",") + ";")
                sb.append("${pedido.metodoPago?.valor ?: "No definido"}\n")
            }
        }
        return sb.toString()
    }
}
