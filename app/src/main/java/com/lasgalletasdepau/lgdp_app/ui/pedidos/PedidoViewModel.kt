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

    // Usuario logueado (Mozo)
    private val _usuarioLogueado = MutableStateFlow<UsuarioEntity?>(null)
    val usuarioLogueado: StateFlow<UsuarioEntity?> = _usuarioLogueado

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
        cargarUsuario()
        observarPedidosActivos()
    }

    private fun observarPedidosActivos() {
        viewModelScope.launch {
            while(true) {
                try {
                    syncManager.sincronizarTodo()
                } catch(e: Exception) {}

                val lista = appDao.obtenerPedidosActivosGenerales()
                val resultado = lista.map { pedido ->
                    PedidoConDetalles(pedido, appDao.obtenerDetallesPorPedido(pedido.pedidoId))
                }
                _pedidosActivos.value = resultado
                delay(10000)
            }
        }
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            _usuarioLogueado.value = appDao.obtenerUsuarioLogueado()
        }
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

            // 1. Si controla stock, primero vemos si hay stock físico ya preparado
            if (producto.controlaStock) {
                if (nuevaCantidad > producto.stock) {
                    // Si ya no hay stock preparado, intentamos ver si se puede "hacer" más con insumos? 
                    // El usuario dice: "mas no aumentar porque ya no queda harina"
                    // Interpretación: Si hay stock, se vende. Si se acaba el stock, se bloquea si no hay insumos.
                    // Pero usualmente si "controlaStock" es true, es que es algo ya hecho.
                    _errorEvent.emit("Stock insuficiente de ${producto.nombre}")
                    return@launch
                }
                // Si hay stock preparado, no validamos insumos (ya se usaron)
            } else {
                // 2. Si NO controla stock (ej. Café hecho al momento), validamos insumos
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

            // 3. Agregar al carrito
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

    fun actualizarComentario(productoId: String, comentario: String) {
        // Mantenemos la función para no romper compatibilidad, pero ya no se usa en UI
    }

    fun actualizarNotasGlobales(notas: String) {
        _notasGlobales.value = notas
    }

    fun guardarPedido(mesaId: Int?, clienteNombre: String?, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val user = _usuarioLogueado.value
            val idFinal = pedidoExistenteId ?: UUID.randomUUID().toString()
            val total = _carrito.value.values.sumOf { it.cantidad * it.precioUnitario }

            // Buscar si hay una caja abierta para anexar el pedido
            val cajaAbierta = appDao.obtenerCajaAbiertaSync()

            val pedidoActual = if (pedidoExistenteId != null) {
                appDao.obtenerPedidoPorId(idFinal)?.copy(
                    total = total,
                    usuarioId = user?.uid,
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId, // Se asocia a la caja actual
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
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId, // Se asocia a la caja actual
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
