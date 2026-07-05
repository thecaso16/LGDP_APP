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

    // Si estamos editando un pedido existente
    private var pedidoExistenteId: String? = null

    // Lista de pedidos activos (para la nueva vista solicitada)
    private val _pedidosActivos = MutableStateFlow<List<PedidoConDetalles>>(emptyList())
    val pedidosActivos: StateFlow<List<PedidoConDetalles>> = _pedidosActivos

    init {
        cargarUsuario()
        observarPedidosActivos()
    }

    private fun observarPedidosActivos() {
        viewModelScope.launch {
            while(true) {
                val lista = appDao.obtenerPedidosActivosGenerales()
                val resultado = lista.map { pedido ->
                    PedidoConDetalles(pedido, appDao.obtenerDetallesPorPedido(pedido.pedidoId))
                }
                _pedidosActivos.value = resultado
                delay(5000)
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

    // Carga un pedido existente para edición
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
                // Actualizar notas si estuvieran en BD
            }
        }
    }

    fun agregarProducto(producto: ProductoEntity) {
        val currentCarrito = _carrito.value.toMutableMap()
        val detalleExistente = currentCarrito[producto.productoId]

        if (detalleExistente != null) {
            if (detalleExistente.cantidad < producto.stock) {
                currentCarrito[producto.productoId] = detalleExistente.copy(
                    cantidad = detalleExistente.cantidad + 1
                )
            }
        } else {
            if (producto.stock > 0) {
                currentCarrito[producto.productoId] = PedidoDetalleEntity(
                    pedidoId = pedidoExistenteId ?: "", 
                    productoId = producto.productoId,
                    nombreProducto = producto.nombre,
                    cantidad = 1,
                    precioUnitario = producto.precio,
                    comentario = ""
                )
            }
        }
        _carrito.value = currentCarrito
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
        val currentCarrito = _carrito.value.toMutableMap()
        val detalle = currentCarrito[productoId] ?: return
        currentCarrito[productoId] = detalle.copy(comentario = comentario)
        _carrito.value = currentCarrito
    }

    fun actualizarNotasGlobales(notas: String) {
        _notasGlobales.value = notas
    }

    fun guardarPedido(mesaId: Int?, clienteNombre: String?, onCompletado: () -> Unit) {
        viewModelScope.launch {
            val user = _usuarioLogueado.value
            val idFinal = pedidoExistenteId ?: UUID.randomUUID().toString()
            val total = _carrito.value.values.sumOf { it.cantidad * it.precioUnitario }

            val pedidoActual = if (pedidoExistenteId != null) {
                appDao.obtenerPedidoPorId(idFinal)?.copy(
                    total = total,
                    usuarioId = user?.uid, // Asegurar que tenga el ID del usuario actual
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

            limpiarCarrito()
            onCompletado()
        }
    }
}
