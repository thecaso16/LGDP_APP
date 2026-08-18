package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class CajaViewModel(application: Application) : AndroidViewModel(application) {
    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepository)

    // Observamos el usuario de la base de datos local como un Flow para que la UI sea reactiva
    val usuarioLogueado: StateFlow<Usuario?> = usuarioRepository.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val esCajero: StateFlow<Boolean> = usuarioLogueado.map { user ->
        if (user == null) false
        else {
            val roles = RolUsuario.fromStringList(user.rol)
            roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cajaSesion: StateFlow<CajaSesion?> = pedidoRepository.obtenerCajaAbierta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val efectivoSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else pedidoRepository.observarIngresosCaja(MetodoPago.EFECTIVO, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val billeteraDigitalSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else pedidoRepository.observarIngresosCaja(MetodoPago.BILLETERA_DIGITAL, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val izipaySistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else pedidoRepository.observarIngresosCaja(MetodoPago.IZIPAY, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val montoApertura = MutableStateFlow("")
    val egresos = MutableStateFlow("")
    val justificacionEgresos = MutableStateFlow("")
    val montoRealFisico = MutableStateFlow("")

    init {
        verificarYRecuperarPerfil()
    }

    private fun verificarYRecuperarPerfil() {
        viewModelScope.launch {
            val user = usuarioRepository.obtenerUsuarioLogueadoSync()
            if (user == null) {
                // Si no hay sesión local, intentamos recuperar del remoto si hay Firebase Auth
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val remoteUser = usuarioRepository.recuperarDatosUsuarioRemoto(currentUser.uid)
                    if (remoteUser != null) {
                        usuarioRepository.loginLocal(remoteUser)
                    }
                }
            }
        }
    }

    fun tieneRolCajero(): Boolean {
        val user = usuarioLogueado.value ?: return false
        val roles = RolUsuario.fromStringList(user.rol)
        return roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
    }

    fun abrirCaja() {
        val user = usuarioLogueado.value ?: return
        val monto = montoApertura.value.toDoubleOrNull() ?: 0.0
        
        viewModelScope.launch {
            val nuevaSesion = CajaSesion(
                cajaId = UUID.randomUUID().toString(),
                usuarioCajeroId = user.id,
                nombreCajero = "${user.nombres} ${user.apellidos}",
                fechaApertura = System.currentTimeMillis(),
                montoApertura = monto,
                estado = "ABIERTA"
            )
            pedidoRepository.abrirCaja(nuevaSesion)
        }
    }

    suspend fun finalizarCierre(justificacionDescuadre: String? = null): Boolean {
        val sesion = cajaSesion.value ?: return false
        
        val totalVentas = efectivoSistema.value + billeteraDigitalSistema.value + izipaySistema.value
        val apert = sesion.montoApertura
        val egre = egresos.value.toDoubleOrNull() ?: 0.0
        val justEgre = justificacionEgresos.value
        val fisic = montoRealFisico.value.toDoubleOrNull() ?: 0.0
        
        val esperadoFisico = apert + efectivoSistema.value - egre
        val diferencia = fisic - esperadoFisico
        val ahora = System.currentTimeMillis()

        val detalle = CajaDetalle(
            cajaId = sesion.cajaId,
            fechaCierre = ahora,
            egresos = egre,
            ingresosEfectivo = efectivoSistema.value,
            ingresosIzipay = izipaySistema.value,
            ingresosBilleteraDigital = billeteraDigitalSistema.value,
            totalVentas = totalVentas,
            esperadoFisico = esperadoFisico,
            montoFisicoReal = fisic,
            diferencia = diferencia,
            justificacion = "Egresos: $justEgre | Descuadre: ${justificacionDescuadre ?: ""}"
        )

        val exito = pedidoRepository.cerrarCaja(sesion, detalle)
        if (exito) {
            montoApertura.value = ""
            egresos.value = ""
            justificacionEgresos.value = ""
            montoRealFisico.value = ""
        }
        return exito
    }
}
