package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoDetalleEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _usuarioLogueado = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioLogueado: StateFlow<UsuarioEntity?> = _usuarioLogueado

    private val _historial = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val historial: StateFlow<List<PedidoConDetalles>> = _historial

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            _usuarioLogueado.value = appDao.obtenerUsuarioLogueado()
        }
    }

    fun esCajeroOAdmin(): Boolean {
        val roles = RolUsuario.fromStringList(_usuarioLogueado.value?.rol)
        return roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
    }

    fun buscarPorRango(fechaInicioStr: String, fechaFinStr: String) {
        val user = _usuarioLogueado.value ?: return
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

                val verTodo = if (esCajeroOAdmin()) 1 else 0

                // 1. Descargar historial de Firebase si es necesario
                syncManager.bajarHistorialRango(user.uid, calInicio.timeInMillis, calFin.timeInMillis)

                // 2. Obtener de local
                val pedidos = appDao.obtenerPedidosHistorial(user.uid, calInicio.timeInMillis, calFin.timeInMillis, verTodo)
                
                val resultado = pedidos.map { pedido ->
                    val detalles = appDao.obtenerDetallesPorPedido(pedido.pedidoId)
                    PedidoConDetalles(pedido, detalles)
                }
                _historial.value = resultado
            } catch (e: Exception) {
                _historial.value = emptyList()
            }
        }
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