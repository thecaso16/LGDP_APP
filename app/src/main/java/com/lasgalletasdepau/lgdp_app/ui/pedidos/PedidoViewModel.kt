package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import com.lasgalletasdepau.lgdp_app.data.repository.PedidoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.ProductoRepositoryImpl
import com.lasgalletasdepau.lgdp_app.data.repository.UsuarioRepositoryImpl
import com.lasgalletasdepau.lgdp_app.domain.model.*
import com.lasgalletasdepau.lgdp_app.domain.repository.PedidoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.ProductoRepository
import com.lasgalletasdepau.lgdp_app.domain.repository.UsuarioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class PedidoViewModel(application: Application) : AndroidViewModel(application) {

    private val appDao = AppDatabase.getDatabase(application).appDao()
    private val syncManager = SyncManager.getInstance(application)
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(appDao)
    private val pedidoRepository: PedidoRepository = PedidoRepositoryImpl(appDao, syncManager, usuarioRepository)
    private val productoRepository: ProductoRepository = ProductoRepositoryImpl(appDao, syncManager)

    // Observar al usuario de forma reactiva
    val usuarioLogueado: StateFlow<Usuario?> = usuarioRepository.obtenerUsuarioLogueado()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Categorías y Productos
    val categorias: StateFlow<List<Categoria>> = productoRepository.obtenerCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productos: StateFlow<List<Producto>> = productoRepository.obtenerProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Carrito: productId -> PedidoDetalle
    private val _carrito = MutableStateFlow<Map<String, PedidoDetalle>>(emptyMap())
    val carrito: StateFlow<Map<String, PedidoDetalle>> = _carrito

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
                    pedidoRepository.sincronizarPedidosYEstado()
                    val user = usuarioLogueado.value
                    if (user != null) {
                        actualizarPedidosActivos(user)
                    }
                } catch(e: Exception) {}
                delay(10000)
            }
        }
    }

    private suspend fun actualizarPedidosActivos(user: Usuario) {
        // Por defecto mozos solo ven sus pedidos activos. 
        // Si se requiere que vean todos, cambiar verTodo a false.
        val lista = pedidoRepository.obtenerPedidosActivos(user.id, false)
        val resultado = lista.map { pedido ->
            PedidoConDetalles(pedido, pedido.detalles)
        }
        _pedidosActivos.value = resultado
    }

    fun limpiarCarrito() {
        pedidoExistenteId = null
        _carrito.value = emptyMap()
        _notasGlobales.value = ""
    }

    fun cancelarPedido(mesaId: Int?) {
        viewModelScope.launch {
            // Si es un pedido nuevo (no estamos editando uno existente) y hay una mesa, la liberamos
            if (pedidoExistenteId == null && mesaId != null) {
                pedidoRepository.liberarMesa(mesaId)
                pedidoRepository.sincronizarPedidosYEstado()
            }
            limpiarCarrito()
        }
    }

    fun cargarPedidoParaEdicion(mesaId: Int? = null, pedidoId: String? = null) {
        viewModelScope.launch {
            val pedido = if (pedidoId != null) {
                pedidoRepository.obtenerPedidoPorId(pedidoId)
            } else if (mesaId != null) {
                pedidoRepository.obtenerPedidoActivoPorMesa(mesaId)
            } else null

            if (pedido != null) {
                pedidoExistenteId = pedido.pedidoId
                val nuevoCarrito = pedido.detalles.associateBy({ it.productoId ?: UUID.randomUUID().toString() }, { it })
                _carrito.value = nuevoCarrito
                _notasGlobales.value = pedido.notas ?: ""
            }
        }
    }

    fun agregarProducto(producto: Producto) {
        viewModelScope.launch {
            val currentCarrito = _carrito.value.toMutableMap()
            val detalleExistente = currentCarrito[producto.id]
            val nuevaCantidad = (detalleExistente?.cantidad ?: 0) + 1

            if (producto.controlaStock) {
                if (nuevaCantidad > producto.stock) {
                    _errorEvent.emit("Stock insuficiente de ${producto.nombre}")
                    return@launch
                }
            } else {
                val relaciones = productoRepository.obtenerInsumosPorProducto(producto.id)
                if (relaciones.isNotEmpty()) {
                    val todosInsumos = productoRepository.obtenerInsumos().first()
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
                currentCarrito[producto.id] = detalleExistente.copy(cantidad = nuevaCantidad)
            } else {
                currentCarrito[producto.id] = PedidoDetalle(
                    productoId = producto.id,
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

            val cajaAbierta = pedidoRepository.obtenerCajaAbierta().first()
            val ahora = System.currentTimeMillis()

            val pedidoActual = if (pedidoExistenteId != null) {
                val pExistente = pedidoRepository.obtenerPedidoPorId(idFinal)
                pExistente?.copy(
                    total = total,
                    usuarioId = pExistente.usuarioId ?: user?.id,
                    usuarioNombre = pExistente.usuarioNombre ?: "${user?.nombres} ${user?.apellidos}",
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId,
                    detalles = _carrito.value.values.toList()
                )
            } else {
                val siguienteNumero = pedidoRepository.obtenerUltimoNumeroPedidoDelDia() + 1

                Pedido(
                    pedidoId = idFinal,
                    numeroPedido = siguienteNumero,
                    fecha = ahora,
                    estado = EstadoPedido.PENDIENTE,
                    tipoPedido = if (mesaId != null) TipoPedido.EN_MESA else TipoPedido.PARA_LLEVAR,
                    mesaId = mesaId,
                    metodoPago = null,
                    nombreCliente = clienteNombre,
                    total = total,
                    usuarioId = user?.id,
                    usuarioNombre = "${user?.nombres} ${user?.apellidos}",
                    notas = _notasGlobales.value,
                    cajaId = cajaAbierta?.cajaId,
                    detalles = _carrito.value.values.toList()
                )
            }

            if (pedidoActual != null) {
                if (pedidoExistenteId != null) {
                    pedidoRepository.actualizarPedido(pedidoActual)
                } else {
                    pedidoRepository.crearPedido(pedidoActual)
                }

                if (mesaId != null && clienteNombre != null && pedidoExistenteId == null) {
                    pedidoRepository.marcarMesaOcupada(mesaId, clienteNombre)
                }
            }

            pedidoRepository.sincronizarPedidosYEstado()
            limpiarCarrito()
            onCompletado()
        }
    }
}
