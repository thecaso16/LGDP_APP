package com.lasgalletasdepau.lgdp_app.ui.mesas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.ProductoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.CajaSesion
import com.lasgalletasdepau.lgdp_app.domain.model.Mesa
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalonViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepo = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepo)
    private val productoRepository: ProductoRepository = ProductoRepositoryImpl(appDao, syncManager)

    // Lee las mesas de SQLite en tiempo real. Si hay un cambio en BD, la UI se actualiza sola.
    val mesas: StateFlow<List<Mesa>> = pedidoRepository.obtenerEstadoMesas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cajaAbierta: StateFlow<CajaSesion?> = pedidoRepository.obtenerCajaAbierta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Al iniciar, intentamos bajar el estado actual de las mesas
        viewModelScope.launch {
            productoRepository.sincronizarCatalogo()
            pedidoRepository.sincronizarPedidosYEstado()
        }
    }

    // Función que se llamará al confirmar el diálogo de apertura de mesa
    fun abrirMesa(idMesa: Int, nombreCliente: String) {
        viewModelScope.launch {
            // Actualizamos la base de datos local
            pedidoRepository.marcarMesaOcupada(idMesa, nombreCliente)
            // Intentamos sincronizar el cambio inmediatamente
            pedidoRepository.sincronizarPedidosYEstado()
        }
    }

    // Nueva función para forzar limpieza manual si una mesa se queda "congelada"
    fun forzarLimpiezaMesa(idMesa: Int) {
        viewModelScope.launch {
            pedidoRepository.liberarMesa(idMesa)
            pedidoRepository.sincronizarPedidosYEstado()
        }
    }
}