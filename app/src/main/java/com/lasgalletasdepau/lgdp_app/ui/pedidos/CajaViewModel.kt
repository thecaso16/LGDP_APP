package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CajaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val _usuarioLogueado = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioLogueado: StateFlow<UsuarioEntity?> = _usuarioLogueado

    // Calculamos el inicio del día una vez
    private val inicioHoy: Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Estados de dinero Reactivos (Observan la BD en tiempo real)
    val efectivoSistema: StateFlow<Double> = appDao.observarIngresosPorMetodoPago(MetodoPago.EFECTIVO.name, inicioHoy)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val yapeSistema: StateFlow<Double> = appDao.observarIngresosPorMetodoPago(MetodoPago.YAPE.name, inicioHoy)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val izipaySistema: StateFlow<Double> = appDao.observarIngresosPorMetodoPago(MetodoPago.IZIPAY.name, inicioHoy)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    val montoApertura = MutableStateFlow("")
    val egresos = MutableStateFlow("")
    val montoRealFisico = MutableStateFlow("")

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            _usuarioLogueado.value = appDao.obtenerUsuarioLogueado()
        }
    }

    suspend fun finalizarCierre(justificacion: String? = null): Boolean {
        val user = _usuarioLogueado.value ?: return false
        val totalVentas = efectivoSistema.value + yapeSistema.value + izipaySistema.value
        val apertura = montoApertura.value.toDoubleOrNull() ?: 0.0
        val egreso = egresos.value.toDoubleOrNull() ?: 0.0
        val fisico = montoRealFisico.value.toDoubleOrNull() ?: 0.0
        
        val esperadoFisico = apertura + efectivoSistema.value - egreso
        val diferencia = fisico - esperadoFisico

        val cierreData = hashMapOf(
            "fecha" to Timestamp(Date()),
            "usuarioId" to user.uid,
            "usuarioNombre" to "${user.nombres} ${user.apellidos}",
            "montoApertura" to apertura,
            "ingresosEfectivo" to efectivoSistema.value,
            "ingresosYape" to yapeSistema.value,
            "ingresosIzipay" to izipaySistema.value,
            "totalVentas" to totalVentas,
            "egresos" to egreso,
            "montoFisicoReal" to fisico,
            "esperadoFisico" to esperadoFisico,
            "diferencia" to diferencia,
            "justificacion" to (justificacion ?: ""),
            "estado" to if (Math.abs(diferencia) < 0.01) "CUADRADO" else "DESCUADRADO"
        )

        val resultado = try {
            firestore.collection("cierres_caja").add(cierreData).await()
            true
        } catch (e: Exception) {
            false
        }

        if (resultado) {
            // LIMPIEZA ABSOLUTA DE DATOS
            montoApertura.value = ""
            egresos.value = ""
            montoRealFisico.value = ""
        }
        
        return resultado
    }
}
