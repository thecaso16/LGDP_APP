package com.lasgalletasdepau.lgdp_app.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
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
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = fechaInicioMillis
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = fechaFinMillis
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }

                // 1. Obtener todos los productos del catálogo para identificar los no vendidos
                val snapshotCatalogo = firestore.collection("productos").get().await()
                val todosLosProductos = snapshotCatalogo.documents.associate { 
                    it.id to (it.getString("nombre") ?: "Desconocido")
                }

                // 2. Obtener pedidos en el rango
                val snapshotPedidos = firestore.collection("pedidos")
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
                    .get().await()

                var ingresos = 0.0
                var pedidosCont = 0
                var canceladosCont = 0
                val conteoProductos = mutableMapOf<String, Int>()
                val metodosMap = mutableMapOf<String, Double>()

                // Inicializar conteo con 0 para todos los productos del catálogo
                todosLosProductos.values.forEach { nombre ->
                    conteoProductos[nombre] = 0
                }

                for (doc in snapshotPedidos.documents) {
                    val estado = doc.getString("estado")
                    if (estado == "PAGADO") {
                        val total = doc.getDouble("total") ?: 0.0
                        ingresos += total
                        pedidosCont++

                        val metodoRaw = doc.getString("metodoPago") ?: "EFECTIVO"
                        val metodoEnum = MetodoPago.fromString(metodoRaw)
                        val metodoNombre = metodoEnum?.valor ?: metodoRaw
                        metodosMap[metodoNombre] = (metodosMap[metodoNombre] ?: 0.0) + total
                            
                        val detalles = doc.get("detalles") as? List<Map<String, Any>>
                        detalles?.forEach { det ->
                            val nombre = det["nombreProducto"] as? String ?: "Desconocido"
                            val cant = (det["cantidad"] as? Long)?.toInt() ?: 0
                            conteoProductos[nombre] = (conteoProductos[nombre] ?: 0) + cant
                        }
                    } else if (estado == "CANCELADO") {
                        canceladosCont++
                    }
                }

                // 3. Obtener cierres de caja para egresos
                val snapshotCierres = firestore.collection("cierres_caja")
                    .whereGreaterThanOrEqualTo("fechaCierre", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fechaCierre", Timestamp(calFin.time))
                    .get().await()
                
                var egresosSum = 0.0
                for (doc in snapshotCierres.documents) {
                    egresosSum += doc.getDouble("egresos") ?: 0.0
                }

                _totalIngresos.value = ingresos
                _totalPedidos.value = pedidosCont
                _totalCancelados.value = canceladosCont
                _totalEgresos.value = egresosSum
                _promedioTicket.value = if (pedidosCont > 0) ingresos / pedidosCont else 0.0
                _ventasPorMetodo.value = metodosMap

                val maxUnidades = conteoProductos.values.maxOrNull() ?: 1
                val allMappedProducts = conteoProductos.entries
                    .map { ProductoEstadistica(it.key, it.value, it.value.toFloat() / maxUnidades) }

                // Top 10: Los más vendidos
                _topProductos.value = allMappedProducts
                    .filter { it.cantidadVendida > 0 }
                    .sortedByDescending { it.cantidadVendida }
                    .take(10)

                // Bottom 10: Los que tienen 0 ventas o las cantidades más bajas
                _bottomProductos.value = allMappedProducts
                    .sortedBy { it.cantidadVendida }
                    .take(10)

            } catch (e: Exception) {
                Log.e("ReportesNegocioVM", "Error: ${e.message}")
                _error.value = "Error al conectar con el servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
