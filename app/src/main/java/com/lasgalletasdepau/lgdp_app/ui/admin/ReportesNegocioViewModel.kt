package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductoEstadistica(
    val nombre: String,
    val cantidadVendida: Int,
    val porcentaje: Float
)

class ReportesNegocioViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepo = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepo)

    private val _totalIngresos = MutableStateFlow(0.0)
    val totalIngresos: StateFlow<Double> = _totalIngresos

    private val _totalPedidos = MutableStateFlow(0)
    val totalPedidos: StateFlow<Int> = _totalPedidos

    private val _totalCancelados = MutableStateFlow(0)
    val totalCancelados: StateFlow<Int> = _totalCancelados

    private val _totalEgresos = MutableStateFlow(0.0)
    val totalEgresos: StateFlow<Double> = _totalEgresos

    private val _topProductos = MutableStateFlow<List<ProductoEstadistica>>(emptyList())
    val topProductos: StateFlow<List<ProductoEstadistica>> = _topProductos

    private val _bottomProductos = MutableStateFlow<List<ProductoEstadistica>>(emptyList())
    val bottomProductos: StateFlow<List<ProductoEstadistica>> = _bottomProductos

    private val _ventasPorMetodo = MutableStateFlow<Map<String, Double>>(emptyMap())
    val ventasPorMetodo: StateFlow<Map<String, Double>> = _ventasPorMetodo

    private val _promedioTicket = MutableStateFlow(0.0)
    val promedioTicket: StateFlow<Double> = _promedioTicket

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarReporte(fechaInicioMillis: Long, fechaFinMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val data = pedidoRepository.obtenerReporteNegocio(fechaInicioMillis, fechaFinMillis)
                
                val ingresos = data["totalIngresos"] as Double
                val pedidosCont = data["totalPedidos"] as Int
                val canceladosCont = data["totalCancelados"] as Int
                val egresosSum = data["totalEgresos"] as Double
                val conteoProductos = data["conteoProductos"] as Map<String, Int>
                val metodosMap = data["ventasPorMetodo"] as Map<String, Double>

                _totalIngresos.value = ingresos
                _totalPedidos.value = pedidosCont
                _totalCancelados.value = canceladosCont
                _totalEgresos.value = egresosSum
                _promedioTicket.value = if (pedidosCont > 0) ingresos / pedidosCont else 0.0
                _ventasPorMetodo.value = metodosMap

                val maxUnidades = conteoProductos.values.maxOrNull() ?: 1
                val allMappedProducts = conteoProductos.entries
                    .map { ProductoEstadistica(it.key, it.value, it.value.toFloat() / maxUnidades) }

                _topProductos.value = allMappedProducts
                    .filter { it.cantidadVendida > 0 }
                    .sortedByDescending { it.cantidadVendida }
                    .take(10)

                _bottomProductos.value = allMappedProducts
                    .sortedBy { it.cantidadVendida }
                    .take(10)

            } catch (e: Exception) {
                _error.value = "Error al conectar con el servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
