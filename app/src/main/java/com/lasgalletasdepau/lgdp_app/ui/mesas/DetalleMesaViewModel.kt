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
    private val syncManager = com.lasgalletasdepau.lgdp_app.data.remote.SyncManager.getInstance(application)

    private val _pedido = MutableStateFlow<PedidoEntity?>(null)
    val pedido: StateFlow<PedidoEntity?> = _pedido

    private val _detalles = MutableStateFlow<List<PedidoDetalleEntity>>(emptyList())
    val detalles: StateFlow<List<PedidoDetalleEntity>> = _detalles

    private var currentMesaId: Int? = null

    fun cargarDatosMesa(mesaId: Int? = null, pedidoId: String? = null) {
        currentMesaId = mesaId
        viewModelScope.launch {
            val pedidoActivo = if (pedidoId != null) {
                appDao.obtenerPedidoPorId(pedidoId)
            } else if (mesaId != null) {
                appDao.obtenerPedidoActivoPorMesa(mesaId)
            } else null

            _pedido.value = pedidoActivo
            if (pedidoActivo != null) {
                _detalles.value = appDao.obtenerDetallesPorPedido(pedidoActivo.pedidoId)
            } else {
                _detalles.value = emptyList()
            }
        }
    }

    fun pagarPedido(metodo: MetodoPago, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value ?: return@launch
            appDao.finalizarVenta(p.pedidoId, metodo, p.mesaId)
            syncManager.sincronizarTodo()
            onCompletado()
        }
    }

    fun cancelarPedido(onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value
            if (p != null) {
                // Si hay un pedido activo, lo anulamos (Estado CANCELADO)
                appDao.anularPedido(p.pedidoId)
                p.mesaId?.let { appDao.liberarMesa(it) }
            } else {
                // Si no hay pedido (caso congelado), liberamos la mesa directamente
                currentMesaId?.let { appDao.liberarMesa(it) }
            }
            
            syncManager.sincronizarTodo()
            onCompletado()
        }
    }
}
