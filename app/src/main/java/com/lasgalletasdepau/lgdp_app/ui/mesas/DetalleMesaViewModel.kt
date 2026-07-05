package com.lasgalletasdepau.lgdp_app.ui.mesas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoDetalleEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.PedidoEntity
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetalleMesaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()

    private val _pedido = MutableStateFlow<PedidoEntity?>(null)
    val pedido: StateFlow<PedidoEntity?> = _pedido

    private val _detalles = MutableStateFlow<List<PedidoDetalleEntity>>(emptyList())
    val detalles: StateFlow<List<PedidoDetalleEntity>> = _detalles

    fun cargarDatosMesa(mesaId: Int? = null, pedidoId: String? = null) {
        viewModelScope.launch {
            val pedidoActivo = if (pedidoId != null) {
                appDao.obtenerPedidoPorId(pedidoId)
            } else if (mesaId != null) {
                appDao.obtenerPedidoActivoPorMesa(mesaId)
            } else null

            _pedido.value = pedidoActivo
            if (pedidoActivo != null) {
                _detalles.value = appDao.obtenerDetallesPorPedido(pedidoActivo.pedidoId)
            }
        }
    }

    fun pagarPedido(metodo: MetodoPago, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value ?: return@launch
            appDao.finalizarVenta(p.pedidoId, metodo.name, p.mesaId)
            onCompletado()
        }
    }

    fun cancelarPedido(onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value ?: return@launch
            // En un flujo real, podrías marcar como CANCELADO
            appDao.actualizarEstadoPedido(p.pedidoId, "CANCELADO", "NINGUNO")
            p.mesaId?.let { appDao.liberarMesa(it) }
            onCompletado()
        }
    }
}
