package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.ProductoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.Insumo
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GestionInventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val productoRepository: ProductoRepository = ProductoRepositoryImpl(appDao, syncManager)

    val insumos: StateFlow<List<Insumo>> = productoRepository.obtenerInsumos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun guardarInsumo(insumo: Insumo, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.guardarInsumo(insumo)
                productoRepository.sincronizarCatalogo()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarInsumo(insumoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.eliminarInsumo(insumoId)
                productoRepository.sincronizarCatalogo()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
