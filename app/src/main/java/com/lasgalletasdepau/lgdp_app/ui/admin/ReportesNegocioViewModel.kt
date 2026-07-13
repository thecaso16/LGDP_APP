package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class ProductoEstadistica(
    val nombre: String,
    val cantidadVendida: Int,
    val porcentaje: Float
)

class ReportesNegocioViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _totalIngresos = MutableStateFlow(0.0)
    val totalIngresos: StateFlow<Double> = _totalIngresos

    private val _totalPedidos = MutableStateFlow(0)
    val totalPedidos: StateFlow<Int> = _totalPedidos

    private val _topProductos = MutableStateFlow<List<ProductoEstadistica>>(emptyList())
    val topProductos: StateFlow<List<ProductoEstadistica>> = _topProductos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun cargarReporte(fechaInicioMillis: Long, fechaFinMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // NORMALIZAR FECHAS:
                // Inicio: 00:00:00.000
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = fechaInicioMillis
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Fin: 23:59:59.999
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = fechaFinMillis
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                val snapshot = firestore.collection("pedidos")
                    .whereEqualTo("estado", "PAGADO")
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
                    .get().await()

                var ingresos = 0.0
                var pedidosCont = 0
                val conteoProductos = mutableMapOf<String, Int>()

                for (doc in snapshot.documents) {
                    ingresos += doc.getDouble("total") ?: 0.0
                    pedidosCont++
                        
                    val detalles = doc.get("detalles") as? List<Map<String, Any>>
                    detalles?.forEach { det ->
                        val nombre = det["nombreProducto"] as? String ?: "Desconocido"
                        val cant = (det["cantidad"] as? Long)?.toInt() ?: 0
                        conteoProductos[nombre] = (conteoProductos[nombre] ?: 0) + cant
                    }
                }

                _totalIngresos.value = ingresos
                _totalPedidos.value = pedidosCont

                val maxUnidades = conteoProductos.values.maxOrNull() ?: 1
                _topProductos.value = conteoProductos.entries
                    .map { ProductoEstadistica(it.key, it.value, it.value.toFloat() / maxUnidades) }
                    .sortedByDescending { it.cantidadVendida }

            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
