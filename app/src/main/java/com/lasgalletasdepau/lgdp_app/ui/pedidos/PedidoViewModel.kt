package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoPedido
import com.lasgalletasdepau.lgdp_app.domain.model.TipoPedido
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class PedidoViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = com.lasgalletasdepau.lgdp_app.data.remote.SyncManager.getInstance(application)

    // Observar al usuario de forma reactiva
    val usuarioLogueado: StateFlow<UsuarioEntity?> = appDao.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Categorías y Productos
    val categorias: StateFlow<List<CategoriaEntity>> = appDao.obtenerCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productos: StateFlow<List<ProductoEntity>> = appDao.obtenerProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Carrito: productId -> PedidoDetalleEntity
    private val _carrito = MutableStateFlow<Map<String, PedidoDetalleEntity>>(emptyMap())
    val carrito: StateFlow<Map<String, PedidoDetalleEntity>> = _carrito

    private val _notasGlobales = MutableStateFlow("")
    val notasGlobales: StateFlow<String> = _notasGlobales

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent

    // Si estamos editando un pedido existente
    private var pedidoExistenteId: String? = null

    // Lista de pedidos activos
    private val _pedidosActivos = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val pedidosActivos: StateFlow<List<PedidoConDetalles>> = _pedidosActivos

    init {
        observarPedidosActivos()
    }

    private fun observarPedidosActivos() {
        viewModelScope.launch {
            // Esperar a que el usuario esté cargado para filtrar correctamente
            usuarioLogueado.collect { user ->
                if (user != null) {
                    actualizarPedidosActivos(user)
                }
            }
        }
        
        // Loop de sincronización periódica
        viewModelScope.launch {
            while(true) {
                try {
                    syncManager.sincronizarPedidosYEstado()
                    val user = usuarioLogueado.value
                    if (user != null) {
                        actualizarPedidosActivos(user)
                    }
                } catch(e: Exception) {}
                delay(10000)
            }
        }
    }

    private suspend fun actualizarPedidosActivos(user: UsuarioEntity) {
        // Por defecto mozos solo ven sus pedidos activos. 
        // Si se requiere que vean todos, cambiar verTodo a 1.
        val lista = appDao.obtenerPedidosActivos(user.uid, 0)
        val resultado = lista.map { pedido ->
            PedidoConDetalles(pedido, appDao.obtenerDetallesPorPedido(pedido.pedidoId))
        }
        _pedidosActivos.value = resultado
    }

    fun limpiarCarrito() {
        pedidoExistenteId = null
        _carrito.value = emptyMap()
        _notasGlobales.value = ""
    }

    fun cargarPedidoParaEdicion(mesaId: Int? = null, pedidoId: String? = null) {
        viewModelScope.launch {
            val pedido = if (pedidoId != null) {
                appDao.obtenerPedidoPorId(pedidoId)
            } else if (mesaId != null) {
                appDao.obtenerPedidoActivoPorMesa(mesaId)
            } else null

            if (pedido != null) {
                pedidoExistenteId = pedido.pedidoId
                val detalles = appDao.obtenerDetallesPorPedido(pedido.pedidoId)
                
                val nuevoCarrito = detalles.associateBy({ it.productoId ?: UUID.randomUUID().toString() }, { it })
                _carrito.value = nuevoCarrito
                _notasGlobales.value = pedido.notas ?: ""
            }
        }
    }

    fun agregarProducto(producto: ProductoEntity) {
        viewModelScope.launch {
            val currentCarrito = _carrito.value.toMutableMap()
            val detalleExistente = currentCarrito[producto.productoId]
            val nuevaCantidad = (detalleExistente?.cantidad ?: 0) + 1

            if (producto.controlaStock) {
                if (nuevaCantidad > producto.stock) {
                    _errorEvent.emit("Stock insuficiente de ${producto.nombre}")
                    return@launch
                }
            } else {
                val relaciones = appDao.obtenerInsumosPorProducto(producto.productoId)
                if (relaciones.isNotEmpty()) {
                    val todosInsumos = appDao.obtenerInsumos().first()
                    for (rel in relaciones) {
                        val insumo = todosInsumos.find { it.id == rel.insumoId }
                        if (insumo != null) {
                            val cantidadRequeridaTotal = rel.cantidadRequerida * nuevaCantidad
                            if (insumo.cantidadActual < cantidadRequeridaTotal) {
                                _errorEvent.emit("Falta insumo: ${insumo.nombre}")
                                return@launch
                            }
                        }
                    }
                }
            }

            if (detalleExistente != null) {
                currentCarrito[producto.productoId] = detalleExistente.copy(cantidad = nuevaCantidad)
            } else {
                currentCarrito[producto.productoId] = PedidoDetalleEntity(
                    pedidoId = pedidoExistenteId ?: "", 
                    productoId = producto.productoId,
                    nombreProducto = producto.nombre,
                    cantidad = 1,
                    precioUnitario = producto.precio,
                    comentario = ""
                )
            }
            _carrito.value = currentCarrito
        }
    }

    fun quitarProducto(productoId: String) {
        val currentCarrito = _carrito.value.toMutableMap()
        val detalleExistente = currentCarrito[productoId] ?: return

        if (detalleExistente.cantidad > 1) {
            currentCarrito[productoId] = detalleExistente.copy(
                cantidad = detalleExistente.cantidad - 1
            )
        } else {
            currentCarrito.remove(productoId)
        }
        _carrito.value = currentCarrito
    }

    fun actualizarNotasGlobales(notas: String) {
        _notasGlobales.value = notas
    }

    fun guardarPedido(mesaId: Int?, clienteNombre: String?, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val user = usuarioLogueado.value
            val idFinal = pedidoExistenteId ?: UUID.randomUUID().toString()
            val total = _carrito.value.values.sumOf { it.cantidad * it.precioUnitario }

            val cajaAbierta = appDao.obtenerCajaAbiertaSync()

            val pedidoActual = if (pedidoExistenteId != null) {
                val pExistente = appDao.obtenerPedidoPorId(idFinal)
                pExistente?.copy(
                    total = total,
                    usuarioId = pExistente.usuarioId ?: user?.uid,
                    usuarioNombre = pExistente.usuarioNombre ?: "${user?.nombres} ${user?.apellidos}",
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId,
                    sincronizado = false
                )
            } else {
                PedidoEntity(
                    pedidoId = idFinal,
                    numeroPedido = (System.currentTimeMillis() % 10000).toInt(),
                    fecha = System.currentTimeMillis(),
                    estado = EstadoPedido.PENDIENTE,
                    tipoPedido = if (mesaId != null) TipoPedido.EN_MESA else TipoPedido.PARA_LLEVAR,
                    mesaId = mesaId,
                    metodoPago = null,
                    nombreCliente = clienteNombre,
                    total = total,
                    usuarioId = user?.uid,
                    usuarioNombre = "${user?.nombres} ${user?.apellidos}",
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId,
                    sincronizado = false
                )
            }

            if (pedidoActual != null) {
                appDao.insertarPedido(pedidoActual)
                appDao.eliminarDetallesPorPedido(idFinal)
                val detalles = _carrito.value.values.map { it.copy(pedidoId = idFinal) }
                appDao.insertarDetallesPedido(detalles)

                if (mesaId != null && clienteNombre != null && pedidoExistenteId == null) {
                    appDao.marcarMesaOcupada(mesaId, clienteNombre)
                }
            }

            syncManager.sincronizarTodo()
            limpiarCarrito()
            onCompletado()
        }
    }
}
