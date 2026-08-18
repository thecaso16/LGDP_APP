package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.PedidoConDetalles
import com.lasgalletasdepau.lgdp_app.domain.model.TrabajadorEstadistica
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DesempenoPedidosViewModel(application: Application) : AndroidViewModel(application) {

    private val pedidoRepository: PedidoRepository

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        val syncManager = SyncManager.getInstance(application)
        val usuarioRepo = UsuarioRepositoryImpl(appDao)
        pedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepo)
    }

    private val _estadisticasTrabajadores = MutableStateFlow<List<TrabajadorEstadistica>>(emptyList())
    val estadisticasTrabajadores: StateFlow<List<TrabajadorEstadistica>> = _estadisticasTrabajadores

    private val _pedidosTrabajadorSeleccionado = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val pedidosTrabajadorSeleccionado: StateFlow<List<PedidoConDetalles>> = _pedidosTrabajadorSeleccionado

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarEstadisticas(inicioMillis: Long, finMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val stats = pedidoRepository.obtenerEstadisticasTrabajadores(inicioMillis, finMillis)
                _estadisticasTrabajadores.value = stats
            } catch (e: Exception) {
                Log.e("DesempenoPedidosVM", "Error: ${e.message}")
                _error.value = "Error al obtener datos del servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga los pedidos específicos de un trabajador.
     */
    fun cargarPedidosTrabajador(usuarioId: String, inicioMillis: Long, finMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pedidos = pedidoRepository.obtenerPedidosConDetallesPorTrabajador(usuarioId, inicioMillis, finMillis)
                _pedidosTrabajadorSeleccionado.value = pedidos
            } catch (e: Exception) {
                Log.e("DesempenoVM", "Error al cargar pedidos trabajador: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun limpiarPedidosTrabajador() {
        _pedidosTrabajadorSeleccionado.value = emptyList()
    }
}
