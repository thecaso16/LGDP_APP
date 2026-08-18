package com.lasgalletasdepau.lgdp_app.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.lasgalletasdepau.lgdp_app.domain.model.*

/**
 * Data Transfer Objects (DTOs) para manejar la deserialización automática de Firestore.
 * Estos objetos se encargan de la compatibilidad con los tipos de Firebase (como Timestamp)
 * y permiten usar toObject() sin ensuciar los modelos de dominio.
 */

data class UsuarioFirestore(
    @DocumentId val firestoreId: String = "",
    val id: String? = null,
    val nombres: String = "",
    val apellidos: String = "",
    val dni: String = "",
    val email: String = "",
    val rol: String = "Trabajador",
    val activo: Boolean = true,
    val creadoEn: Timestamp? = null,
    val ultimaModificacion: Timestamp? = null
) {
    fun toDomain(): Usuario = Usuario(
        id = id ?: firestoreId,
        nombres = nombres,
        apellidos = apellidos,
        dni = dni,
        email = email,
        rol = rol,
        activo = activo,
        creadoEn = creadoEn?.toDate()?.time,
        ultimaModificacion = ultimaModificacion?.toDate()?.time
    )
}

data class PedidoFirestore(
    @DocumentId val firestoreId: String = "",
    val pedidoId: String? = null,
    val numeroPedido: Int = 0,
    val fecha: Timestamp? = null,
    val estado: String = "PENDIENTE",
    val tipoPedido: String = "PARA_LLEVAR",
    val mesaId: Int? = null,
    val metodoPago: String? = null,
    val nombreCliente: String? = null,
    val total: Double = 0.0,
    val usuarioId: String? = null,
    val usuarioNombre: String? = null,
    val notas: String? = null,
    val cajaId: String? = null,
    val detalles: List<PedidoDetalleFirestore> = emptyList()
) {
    fun toDomain(): Pedido = Pedido(
        pedidoId = pedidoId ?: firestoreId,
        numeroPedido = numeroPedido,
        fecha = fecha?.toDate()?.time,
        estado = try { EstadoPedido.valueOf(estado) } catch (e: Exception) { EstadoPedido.PENDIENTE },
        tipoPedido = try { TipoPedido.valueOf(tipoPedido) } catch (e: Exception) { TipoPedido.PARA_LLEVAR },
        mesaId = mesaId,
        metodoPago = MetodoPago.fromString(metodoPago),
        nombreCliente = nombreCliente,
        total = total,
        usuarioId = usuarioId,
        usuarioNombre = usuarioNombre,
        notas = notas,
        cajaId = cajaId,
        detalles = detalles.map { it.toDomain() }
    )
}

data class PedidoDetalleFirestore(
    val productoId: String? = null,
    val nombreProducto: String? = null,
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val comentario: String? = null
) {
    fun toDomain(): PedidoDetalle = PedidoDetalle(
        productoId = productoId,
        nombreProducto = nombreProducto,
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        comentario = comentario
    )
}

data class ProductoFirestore(
    @DocumentId val firestoreId: String = "",
    val id: String? = null,
    val nombre: String = "",
    val descripcion: String? = null,
    val categoriaId: String? = null,
    val precio: Double = 0.0,
    val stock: Int = 0,
    val controlaStock: Boolean = false,
    val estaDisponible: Boolean = true,
    val imagen: String? = null,
    val activo: Boolean = true,
    val recomendado: Boolean = false,
    val ultimaActualizacion: Timestamp? = null
) {
    fun toDomain(): Producto = Producto(
        id = id ?: firestoreId,
        nombre = nombre,
        descripcion = descripcion,
        categoriaId = categoriaId,
        precio = precio,
        stock = stock,
        controlaStock = controlaStock,
        estaDisponible = estaDisponible,
        imagen = imagen,
        activo = activo,
        recomendado = recomendado,
        ultimaActualizacion = ultimaActualizacion?.toDate()?.time
    )
}

data class MesaFirestore(
    @DocumentId val firestoreId: String = "",
    val id: Int? = null,
    val numero: String? = null,
    val estado: String = "LIBRE",
    val clienteActivo: String? = null
) {
    fun toDomain(): Mesa = Mesa(
        id = id ?: firestoreId.toIntOrNull() ?: 0,
        numero = numero ?: "Mesa ${firestoreId.padStart(2, '0')}",
        estado = try { EstadoMesa.valueOf(estado) } catch (e: Exception) { EstadoMesa.LIBRE },
        clienteActivo = clienteActivo
    )
}

data class CategoriaFirestore(
    @DocumentId val firestoreId: String = "",
    val id: String? = null,
    val nombre: String? = null
) {
    fun toDomain(): Categoria = Categoria(id = id ?: firestoreId, nombre = nombre)
}

data class CajaSesionFirestore(
    @DocumentId val firestoreId: String = "",
    val cajaId: String? = null,
    val usuarioCajeroId: String? = null,
    val usuarioId: String? = null, // fallback
    val usuarioCajeroNombre: String? = null,
    val fechaApertura: Timestamp? = null,
    val montoApertura: Double = 0.0,
    val estado: String = "ABIERTA"
) {
    fun toDomain(): CajaSesion = CajaSesion(
        cajaId = cajaId ?: firestoreId,
        usuarioCajeroId = usuarioCajeroId ?: usuarioId ?: "",
        nombreCajero = usuarioCajeroNombre ?: "Desconocido",
        fechaApertura = fechaApertura?.toDate()?.time ?: System.currentTimeMillis(),
        montoApertura = montoApertura,
        estado = estado
    )
}

data class InsumoFirestore(
    @DocumentId val firestoreId: String = "",
    val id: String? = null,
    val nombre: String = "",
    val cantidadActual: Double = 0.0,
    val cantidadMinima: Double = 0.0,
    val unidadMedida: String = "Kg",
    val categoria: String? = null
) {
    fun toDomain(): Insumo = Insumo(
        id = id ?: firestoreId,
        nombre = nombre,
        cantidadActual = cantidadActual,
        cantidadMinima = cantidadMinima,
        unidadMedida = unidadMedida,
        categoria = categoria ?: ""
    )
}

data class ProductoInsumoFirestore(
    val productoId: String = "",
    val insumoId: String = "",
    val cantidadRequerida: Double = 0.0
) {
    fun toDomain(): ProductoInsumo = ProductoInsumo(
        productoId = productoId,
        insumoId = insumoId,
        cantidadRequerida = cantidadRequerida
    )
}
