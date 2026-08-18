package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ModoHistorial {
    TURNO_ACTUAL,
    BUSQUEDA_HISTORICA
}

class ReportesTrabajadoresViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepository)

    // Observar al usuario de forma reactiva
    val usuarioLogueado: StateFlow<Usuario?> = usuarioRepository.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _modo = MutableStateFlow(ModoHistorial.TURNO_ACTUAL)
    val modo: StateFlow<ModoHistorial> = _modo

    private val _historial = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val historial: StateFlow<List<PedidoConDetalles>> = _historial

    fun cambiarModo(nuevoModo: ModoHistorial) {
        _modo.value = nuevoModo
        if (nuevoModo == ModoHistorial.TURNO_ACTUAL) {
            cargarPedidosTurnoActual()
        }
    }

    fun cargarPedidosTurnoActual() {
        val user = usuarioLogueado.value ?: return
        viewModelScope.launch {
            val sesion = pedidoRepository.obtenerCajaAbierta().first()
            if (sesion != null) {
                // Durante el turno actual, todos ven todos los pedidos (verTodo = true)
                actualizarHistorialLocal(user.id, sesion.fechaApertura, System.currentTimeMillis() + 86400000, verTodoManual = true)
            } else {
                _historial.value = emptyList()
            }
        }
    }

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
        return user.id == cajeroIdEnCaja
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
                actualizarHistorialLocal(user.id, calInicio.timeInMillis, calFin.timeInMillis)

                // 2. Descargar historial de Firebase filtrado por este usuario
                pedidoRepository.bajarHistorialRango(user.id, calInicio.timeInMillis, calFin.timeInMillis)

                // 3. Refrescar datos locales
                actualizarHistorialLocal(user.id, calInicio.timeInMillis, calFin.timeInMillis)
            } catch (e: Exception) {
                // Mantener estado actual en caso de error
            }
        }
    }

    private suspend fun actualizarHistorialLocal(uid: String, inicio: Long, fin: Long, verTodoManual: Boolean? = null) {
        // Para este reporte, permitimos ver toda la actividad del negocio (verTodo = true)
        // ya que los trabajadores necesitan ver pedidos de otros compañeros en la misma caja.
        val verTodo = verTodoManual ?: true
        
        val pedidos = pedidoRepository.obtenerPedidosHistorial(uid, inicio, fin, verTodo)
        val resultado = pedidos.map { pedido ->
            PedidoConDetalles(pedido, pedido.detalles)
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
