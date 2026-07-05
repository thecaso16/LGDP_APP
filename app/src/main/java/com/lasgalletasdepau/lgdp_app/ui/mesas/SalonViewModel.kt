package com.lasgalletasdepau.lgdp_app.ui.mesas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalonViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = com.lasgalletasdepau.lgdp_app.data.remote.SyncManager.getInstance(application)

    // Lee las mesas de SQLite en tiempo real. Si hay un cambio en BD, la UI se actualiza sola.
    val mesas: StateFlow<List<MesaEntity>> = appDao.obtenerEstadoMesas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Al iniciar, intentamos bajar el estado actual de las mesas
        viewModelScope.launch {
            syncManager.sincronizarTodo()
        }
    }

    // Función que se llamará al confirmar el diálogo de apertura de mesa
    fun abrirMesa(idMesa: Int, nombreCliente: String) {
        viewModelScope.launch {
            // Actualizamos la base de datos local
            appDao.marcarMesaOcupada(idMesa, nombreCliente)
            // Intentamos sincronizar el cambio inmediatamente
            syncManager.sincronizarTodo()
        }
    }

    // Nueva función para forzar limpieza manual si una mesa se queda "congelada"
    fun forzarLimpiezaMesa(idMesa: Int) {
        viewModelScope.launch {
            appDao.liberarMesa(idMesa)
            syncManager.sincronizarTodo()
        }
    }
}