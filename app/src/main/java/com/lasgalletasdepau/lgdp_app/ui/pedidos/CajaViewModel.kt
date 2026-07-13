package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.CajaSesionEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.MetodoPago
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CajaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val _usuarioLogueado = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioLogueado: StateFlow<UsuarioEntity?> = _usuarioLogueado

    val cajaSesion: StateFlow<CajaSesionEntity?> = appDao.obtenerCajaAbierta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val efectivoSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else appDao.observarIngresosCaja(MetodoPago.EFECTIVO.name, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val yapeSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else appDao.observarIngresosCaja(MetodoPago.YAPE.name, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val izipaySistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else appDao.observarIngresosCaja(MetodoPago.IZIPAY.name, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    fun tieneRolCajero(): Boolean {
        val user = _usuarioLogueado.value ?: return false
        val roles = RolUsuario.fromStringList(user.rol)
        return roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
    }

    fun abrirCaja() {
        val user = _usuarioLogueado.value ?: return
        val monto = montoApertura.value.toDoubleOrNull() ?: 0.0
        
        viewModelScope.launch {
            try {
                // Verificación extra en Firebase antes de abrir para evitar duplicados
                val snapshot = firestore.collection("cierres_caja")
                    .whereEqualTo("estado", "ABIERTA")
                    .get().await()
                
                if (!snapshot.isEmpty) {
                    Log.d("CajaViewModel", "Ya hay una caja abierta globalmente.")
                    return@launch
                }

                val nuevaSesion = CajaSesionEntity(
                    cajaId = UUID.randomUUID().toString(),
                    usuarioCajeroId = user.uid,
                    nombreCajero = "${user.nombres} ${user.apellidos}",
                    fechaApertura = System.currentTimeMillis(),
                    montoApertura = monto,
                    estado = "ABIERTA"
                )
                appDao.abrirCajaLocal(nuevaSesion)
                subirSesionAFirebase(nuevaSesion)
            } catch (e: Exception) {
                Log.e("CajaViewModel", "Error al abrir caja: ${e.message}")
            }
        }
    }

    suspend fun finalizarCierre(justificacion: String? = null): Boolean {
        val user = _usuarioLogueado.value ?: return false
        val sesion = cajaSesion.value ?: return false
        
        val totalVentas = efectivoSistema.value + yapeSistema.value + izipaySistema.value
        val apert = sesion.montoApertura
        val egre = egresos.value.toDoubleOrNull() ?: 0.0
        val fisic = montoRealFisico.value.toDoubleOrNull() ?: 0.0
        
        val esperadoFisico = apert + efectivoSistema.value - egre
        val diferencia = fisic - esperadoFisico

        val ahora = System.currentTimeMillis()

        // Crear el detalle para Room (Normalizado)
        val detalleCaja = com.lasgalletasdepau.lgdp_app.data.local.entity.CajaDetalleEntity(
            cajaId = sesion.cajaId,
            fechaCierre = ahora,
            egresos = egre,
            ingresosEfectivo = efectivoSistema.value,
            ingresosIzipay = izipaySistema.value,
            ingresosYape = yapeSistema.value,
            totalVentas = totalVentas,
            esperadoFisico = esperadoFisico,
            montoFisicoReal = fisic,
            diferencia = diferencia,
            justificacion = justificacion
        )

        val cierreData = hashMapOf(
            "cajaId" to sesion.cajaId,
            "diferencia" to diferencia,
            "egresos" to egre,
            "esperadoFisico" to esperadoFisico,
            "estado" to "CERRADA",
            "fecha" to Timestamp(Date(ahora)),
            "fechaApertura" to Timestamp(Date(sesion.fechaApertura)),
            "fechaCierre" to Timestamp(Date(ahora)),
            "ingresosEfectivo" to efectivoSistema.value,
            "ingresosIzipay" to izipaySistema.value,
            "ingresosYape" to yapeSistema.value,
            "justificacion" to (justificacion ?: ""),
            "montoApertura" to apert,
            "montoFisicoReal" to fisic,
            "totalVentas" to totalVentas,
            "usuarioCajeroId" to sesion.usuarioCajeroId,
            "usuarioCajeroNombre" to sesion.nombreCajero,
            "usuarioId" to user.uid,
            "usuarioNombre" to "${user.nombres} ${user.apellidos}",
            "resultadoBalance" to if (Math.abs(diferencia) < 0.01) "CUADRADO" else "DESCUADRADO"
        )

        return try {
            firestore.collection("cierres_caja").document(sesion.cajaId).set(cierreData).await()
            // Guardar localmente el detalle antes de limpiar (opcional si se limpiatodo)
            appDao.insertarCajaDetalle(detalleCaja)
            appDao.actualizarCajaLocal(sesion.copy(estado = "CERRADA"))
            
            // Limpieza local forzada tras éxito en Firebase para permitir nueva apertura
            appDao.limpiarSesionesLocales()
            appDao.limpiarDetallesLocales()
            
            montoApertura.value = ""
            egresos.value = ""
            montoRealFisico.value = ""
            true
        } catch (e: Exception) {
            Log.e("CajaViewModel", "Error al finalizar cierre: ${e.message}")
            false
        }
    }

    private suspend fun subirSesionAFirebase(sesion: CajaSesionEntity) {
        val data = hashMapOf(
            "cajaId" to sesion.cajaId,
            "usuarioCajeroId" to sesion.usuarioCajeroId,
            "usuarioCajeroNombre" to sesion.nombreCajero,
            "fechaApertura" to Timestamp(Date(sesion.fechaApertura)),
            "montoApertura" to sesion.montoApertura,
            "estado" to "ABIERTA"
        )
        try {
            firestore.collection("cierres_caja").document(sesion.cajaId).set(data).await()
        } catch (e: Exception) {}
    }
}