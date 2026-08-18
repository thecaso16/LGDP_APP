package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.ProductoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.Categoria
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CategoriasViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val productoRepository: ProductoRepository = ProductoRepositoryImpl(appDao, syncManager)

    val categorias: StateFlow<List<Categoria>> = productoRepository.obtenerCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarCategoria(nombre: String) {
        viewModelScope.launch {
            try {
                productoRepository.agregarCategoria(nombre)
                productoRepository.sincronizarCatalogo()
            } catch (e: Exception) {}
        }
    }
}
