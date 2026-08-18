package com.lasgalletasdepau.lgdp_app.ui.mesas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetalleMesaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepo = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepo)

    private val _pedido = MutableStateFlow<Pedido?>(null)
    val pedido: StateFlow<Pedido?> = _pedido

    private val _detalles = MutableStateFlow<List<PedidoDetalle>>(emptyList())
    val detalles: StateFlow<List<PedidoDetalle>> = _detalles

    val cajaAbierta: StateFlow<CajaSesion?> = pedidoRepository.obtenerCajaAbierta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var currentMesaId: Int? = null

    fun cargarDatosMesa(mesaId: Int? = null, pedidoId: String? = null) {
        currentMesaId = mesaId
        viewModelScope.launch {
            val pedidoActivo = if (pedidoId != null) {
                pedidoRepository.obtenerPedidoPorId(pedidoId)
            } else if (mesaId != null) {
                pedidoRepository.obtenerPedidoActivoPorMesa(mesaId)
            } else null

            _pedido.value = pedidoActivo
            if (pedidoActivo != null) {
                _detalles.value = pedidoActivo.detalles
            } else {
                _detalles.value = emptyList()
            }
        }
    }

    fun pagarPedido(metodo: MetodoPago, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value ?: return@launch
            pedidoRepository.finalizarVenta(p.pedidoId, metodo, p.mesaId)
            pedidoRepository.sincronizarPedidosYEstado()
            onCompletado()
        }
    }

    fun cancelarPedido(justificacion: String, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val p = _pedido.value
            if (p != null) {
                // Si hay un pedido activo, lo anulamos (Estado CANCELADO)
                pedidoRepository.anularPedido(p.pedidoId, justificacion)
                p.mesaId?.let { pedidoRepository.liberarMesa(it) }
            } else {
                // Si no hay pedido (caso congelado), liberamos la mesa directamente
                currentMesaId?.let { pedidoRepository.liberarMesa(it) }
            }
            
            pedidoRepository.sincronizarPedidosYEstado()
            onCompletado()
        }
    }
}
