package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.ProductoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class GestionCatalogoViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val productoRepository: ProductoRepository = ProductoRepositoryImpl(appDao, syncManager)

    val productos: StateFlow<List<Producto>> = productoRepository.obtenerProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val insumosDisponibles: StateFlow<List<Insumo>> = productoRepository.obtenerInsumos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun obtenerInsumosRelacionados(productoId: String, onResult: (List<ProductoInsumo>) -> Unit) {
        viewModelScope.launch {
            try {
                val lista = productoRepository.obtenerInsumosPorProducto(productoId)
                onResult(lista)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun guardarVinculoInsumo(productoId: String, insumoId: String, cantidad: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.guardarVinculoInsumo(productoId, insumoId, cantidad)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarVinculoInsumo(productoId: String, insumoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.eliminarVinculoInsumo(productoId, insumoId)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun guardarProducto(producto: Producto, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.guardarProducto(producto)
                productoRepository.sincronizarCatalogo()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun eliminarProductoLogico(productoId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                productoRepository.eliminarProductoLogico(productoId)
                productoRepository.sincronizarCatalogo()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
