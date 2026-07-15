package com.lasgalletasdepau.lgdp_app.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

data class TrabajadorEstadistica(
    val usuarioId: String,
    val nombre: String,
    val totalVendido: Double,
    val cantidadPedidos: Int,
    val porcentajeVentas: Float = 0f
)

class DesempenoPedidosViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _estadisticasTrabajadores = MutableStateFlow<List<TrabajadorEstadistica>>(emptyList())
    val estadisticasTrabajadores: StateFlow<List<TrabajadorEstadistica>> = _estadisticasTrabajadores

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarEstadisticas(inicioMillis: Long, finMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = inicioMillis
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = finMillis
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }

                val snapshot = firestore.collection("pedidos")
                    .whereEqualTo("estado", "PAGADO")
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
                    .get().await()

                val mapaTrabajadores = mutableMapOf<String, Triple<String, Double, Int>>()
                var totalGlobal = 0.0

                for (doc in snapshot.documents) {
                    val uid = doc.getString("usuarioId") ?: "desconocido"
                    val total = doc.getDouble("total") ?: 0.0
                    totalGlobal += total
                        
                    val actual = mapaTrabajadores[uid] ?: Triple("Cargando...", 0.0, 0)
                    mapaTrabajadores[uid] = Triple(actual.first, actual.second + total, actual.third + 1)
                }

                val listaResult = mutableListOf<TrabajadorEstadistica>()
                for ((uid, stats) in mapaTrabajadores) {
                    var nombre = "Usuario: $uid"
                    if (uid != "desconocido") {
                        try {
                            val userDoc = firestore.collection("usuarios").document(uid).get().await()
                            if (userDoc.exists()) {
                                val nombres = userDoc.getString("nombres") ?: ""
                                val apellidos = userDoc.getString("apellidos") ?: ""
                                nombre = "$nombres $apellidos".trim().ifEmpty { nombre }
                            }
                        } catch (e: Exception) {
                            Log.e("DesempenoPedidosVM", "No se pudo obtener datos del usuario $uid")
                        }
                    }
                    
                    listaResult.add(TrabajadorEstadistica(
                        usuarioId = uid,
                        nombre = nombre,
                        totalVendido = stats.second,
                        cantidadPedidos = stats.third,
                        porcentajeVentas = if (totalGlobal > 0) (stats.second / totalGlobal).toFloat() else 0f
                    ))
                }

                _estadisticasTrabajadores.value = listaResult.sortedByDescending { it.totalVendido }

            } catch (e: Exception) {
                Log.e("DesempenoPedidosVM", "Error al cargar estadísticas: ${e.message}", e)
                _error.value = "Error al obtener datos. Verifique sus permisos e índices."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
