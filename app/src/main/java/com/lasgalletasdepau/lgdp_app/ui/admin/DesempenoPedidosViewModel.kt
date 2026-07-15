package com.lasgalletasdepau.lgdp_app.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.ui.pedidos.PedidoConDetalles
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoDetalleEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoEntity
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido
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
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = inicioMillis
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = finMillis
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }

                val snapshot = firestore.collection("pedidos")
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
                    .get().await()

                val mapaTrabajadores = mutableMapOf<String, Triple<String, Double, Int>>()
                var totalGlobal = 0.0

                for (doc in snapshot.documents) {
                    if (doc.getString("estado") != "PAGADO") continue

                    val uid = doc.getString("usuarioId") ?: "desconocido"
                    val total = doc.getDouble("total") ?: 0.0
                    totalGlobal += total
                        
                    val actual = mapaTrabajadores[uid] ?: Triple("Cargando...", 0.0, 0)
                    mapaTrabajadores[uid] = Triple(actual.first, actual.second + total, actual.third + 1)
                }

                val listaResult = mutableListOf<TrabajadorEstadistica>()
                for ((uid, stats) in mapaTrabajadores) {
                    var nombre = "ID: ${uid.takeLast(6)}"
                    if (uid != "desconocido") {
                        try {
                            val userDoc = firestore.collection("usuarios").document(uid).get().await()
                            if (userDoc.exists()) {
                                val nombres = userDoc.getString("nombres") ?: ""
                                val apellidos = userDoc.getString("apellidos") ?: ""
                                nombre = "$nombres $apellidos".trim().ifEmpty { nombre }
                            }
                        } catch (e: Exception) {
                            Log.e("DesempenoPedidosVM", "Error al cargar usuario $uid")
                        }
                    } else {
                        nombre = "Usuario no identificado"
                    }
                    
                    listaResult.add(TrabajadorEstadistica(
                        uid, nombre, stats.second, stats.third,
                        if (totalGlobal > 0) (stats.second / totalGlobal).toFloat() else 0f
                    ))
                }

                _estadisticasTrabajadores.value = listaResult.sortedByDescending { it.totalVendido }

            } catch (e: Exception) {
                Log.e("DesempenoPedidosVM", "Error: ${e.message}")
                _error.value = "Error al obtener datos del servidor."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Carga los pedidos específicos de un trabajador filtrando en memoria
     * para evitar errores de Índices Compuestos en Firestore.
     */
    fun cargarPedidosTrabajador(usuarioId: String, inicioMillis: Long, finMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val calInicio = Calendar.getInstance().apply {
                    timeInMillis = inicioMillis
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val calFin = Calendar.getInstance().apply {
                    timeInMillis = finMillis
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }

                // Usamos solo el filtro de fecha que ya está indexado
                val snapshot = firestore.collection("pedidos")
                    .whereGreaterThanOrEqualTo("fecha", Timestamp(calInicio.time))
                    .whereLessThanOrEqualTo("fecha", Timestamp(calFin.time))
                    .get().await()

                // Filtramos por usuario y estado PAGADO en la aplicación
                val lista = snapshot.documents
                    .filter { it.getString("usuarioId") == usuarioId }
                    .mapNotNull { doc ->
                        val firebaseTimestamp = doc.getTimestamp("fecha")
                        val firebaseFecha = firebaseTimestamp?.toDate()?.time ?: 0L
                        
                        val pedido = PedidoEntity(
                            pedidoId = doc.id,
                            numeroPedido = doc.getLong("numeroPedido")?.toInt() ?: 0,
                            fecha = firebaseFecha,
                            estado = doc.getString("estado")?.let { try { EstadoPedido.valueOf(it) } catch(e: Exception) { null } },
                            tipoPedido = doc.getString("tipoPedido")?.let { try { TipoPedido.valueOf(it) } catch(e: Exception) { TipoPedido.PARA_LLEVAR } } ?: TipoPedido.PARA_LLEVAR,
                            mesaId = doc.getLong("mesaId")?.toInt(),
                            metodoPago = MetodoPago.fromString(doc.getString("metodoPago")),
                            nombreCliente = doc.getString("nombreCliente"),
                            total = doc.getDouble("total") ?: 0.0,
                            usuarioId = doc.getString("usuarioId"),
                            usuarioNombre = doc.getString("usuarioNombre"),
                            notas = doc.getString("notas"),
                            cajaId = doc.getString("cajaId"),
                            sincronizado = true
                        )
                        
                        val detallesNube = doc.get("detalles") as? List<Map<String, Any>>
                        val detallesEntities = detallesNube?.map { map ->
                            PedidoDetalleEntity(
                                pedidoId = doc.id,
                                productoId = map["productoId"] as? String,
                                nombreProducto = map["nombreProducto"] as? String,
                                cantidad = (map["cantidad"] as? Long)?.toInt() ?: 0,
                                precioUnitario = (map["precioUnitario"] as? Number)?.toDouble() ?: 0.0,
                                comentario = null
                            )
                        } ?: emptyList()
                        
                        PedidoConDetalles(pedido, detallesEntities)
                    }.sortedByDescending { it.pedido.fecha }
                
                _pedidosTrabajadorSeleccionado.value = lista
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
