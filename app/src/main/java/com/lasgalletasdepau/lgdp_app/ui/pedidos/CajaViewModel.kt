package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
    private val auth = FirebaseAuth.getInstance()

    // Observamos el usuario de la base de datos local como un Flow para que la UI sea reactiva
    val usuarioLogueado: StateFlow<UsuarioEntity?> = appDao.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val esCajero: StateFlow<Boolean> = usuarioLogueado.map { user ->
        if (user == null) false
        else {
            val roles = RolUsuario.fromStringList(user.rol)
            roles.contains(RolUsuario.CAJERO) || roles.contains(RolUsuario.ADMINISTRADOR)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val cajaSesion: StateFlow<CajaSesionEntity?> = appDao.obtenerCajaAbierta()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val efectivoSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else appDao.observarIngresosCaja(MetodoPago.EFECTIVO.name, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
            .map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val billeteraDigitalSistema: StateFlow<Double> = cajaSesion.flatMapLatest { sesion ->
        if (sesion == null) flowOf(0.0)
        else appDao.observarIngresosCaja(MetodoPago.BILLETERA_DIGITAL.name, sesion.fechaApertura, System.currentTimeMillis() + 86400000)
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
    val justificacionEgresos = MutableStateFlow("")
    val montoRealFisico = MutableStateFlow("")

    init {
        verificarYRecuperarPerfil()
    }

    /**
     * Si el usuario está autenticado en Firebase pero la tabla local está vacía
     * (por ejemplo, al reinstalar o limpiar caché), recuperamos los datos de Firestore.
     */
    private fun verificarYRecuperarPerfil() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val localUser = usuarioLogueado.value
                if (localUser == null) {
                    try {
                        val doc = firestore.collection("usuarios").document(currentUser.uid).get().await()
                        if (doc.exists()) {
                            val user = UsuarioEntity(
                                uid = currentUser.uid,
                                email = doc.getString("email"),
                                nombres = doc.getString("nombres"),
                                apellidos = doc.getString("apellidos"),
                                dni = doc.getString("dni"),
                                rol = doc.getString("rol") ?: "Trabajador",
                                activo = true
                            )
                            appDao.insertarUsuario(user)
                        }
                    } catch (e: Exception) {
                        Log.e("CajaViewModel", "Error recuperando perfil: ${e.message}")
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
            try {
                val snapshot = firestore.collection("cierres_caja")
                    .whereEqualTo("estado", "ABIERTA")
                    .get().await()
                
                if (!snapshot.isEmpty) return@launch

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

    suspend fun finalizarCierre(justificacionDescuadre: String? = null): Boolean {
        val user = usuarioLogueado.value ?: return false
        val sesion = cajaSesion.value ?: return false
        
        val totalVentas = efectivoSistema.value + billeteraDigitalSistema.value + izipaySistema.value
        val apert = sesion.montoApertura
        val egre = egresos.value.toDoubleOrNull() ?: 0.0
        val justEgre = justificacionEgresos.value
        val fisic = montoRealFisico.value.toDoubleOrNull() ?: 0.0
        
        val esperadoFisico = apert + efectivoSistema.value - egre
        val diferencia = fisic - esperadoFisico
        val ahora = System.currentTimeMillis()

        val detalleCaja = com.lasgalletasdepau.lgdp_app.data.local.entity.CajaDetalleEntity(
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

        val cierreData = hashMapOf(
            "cajaId" to sesion.cajaId,
            "diferencia" to diferencia,
            "egresos" to egre,
            "justificacionEgresos" to justEgre,
            "esperadoFisico" to esperadoFisico,
            "estado" to "CERRADA",
            "fecha" to Timestamp(Date(ahora)),
            "fechaApertura" to Timestamp(Date(sesion.fechaApertura)),
            "fechaCierre" to Timestamp(Date(ahora)),
            "ingresosEfectivo" to efectivoSistema.value,
            "ingresosIzipay" to izipaySistema.value,
            "ingresosBilleteraDigital" to billeteraDigitalSistema.value,
            "justificacion" to (justificacionDescuadre ?: ""),
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
            appDao.insertarCajaDetalle(detalleCaja)
            appDao.actualizarCajaLocal(sesion.copy(estado = "CERRADA"))
            appDao.limpiarSesionesLocales()
            appDao.limpiarDetallesLocales()
            
            montoApertura.value = ""
            egresos.value = ""
            justificacionEgresos.value = ""
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
