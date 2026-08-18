package com.lasgalletasdepau.lgdp_app.data.mapper

import com.lasgalletasdepau.lgdp_app.data.local.entity.*
import com.lasgalletasdepau.lgdp_app.domain.model.*

fun MesaEntity.toDomain(): Mesa = Mesa(
    id = id,
    numero = numero,
    estado = estado,
    clienteActivo = clienteActivo
)

fun Mesa.toEntity(sincronizado: Boolean = false): MesaEntity = MesaEntity(
    id = id,
    numero = numero,
    estado = estado,
    clienteActivo = clienteActivo,
    sincronizado = sincronizado
)

fun ProductoEntity.toDomain(): Producto = Producto(
    id = productoId,
    nombre = nombre ?: "",
    descripcion = descripcion,
    categoriaId = categoriaId,
    precio = precio,
    stock = stock,
    controlaStock = controlaStock,
    estaDisponible = estaDisponible,
    imagen = imagen,
    activo = activo,
    recomendado = recomendado,
    ultimaActualizacion = ultimaActualizacion
)

fun Producto.toEntity(sincronizado: Boolean = false): ProductoEntity = ProductoEntity(
    productoId = id,
    nombre = nombre,
    descripcion = descripcion,
    imagen = imagen,
    precio = precio,
    stock = stock,
    controlaStock = controlaStock,
    categoriaId = categoriaId,
    recomendado = recomendado,
    estaDisponible = estaDisponible,
    activo = activo,
    sincronizado = sincronizado,
    ultimaActualizacion = ultimaActualizacion,
    operacionPendiente = null
)

fun UsuarioEntity.toDomain(): Usuario = Usuario(
    id = uid,
    nombres = nombres ?: "",
    apellidos = apellidos ?: "",
    dni = dni ?: "",
    email = email ?: "",
    rol = rol ?: "Trabajador",
    activo = activo,
    creadoEn = creadoEn,
    ultimaModificacion = ultimaModificacion
)

fun Usuario.toEntity(esSesionActual: Boolean = false): UsuarioEntity = UsuarioEntity(
    uid = id,
    nombres = nombres,
    apellidos = apellidos,
    dni = dni,
    email = email,
    rol = rol,
    activo = activo,
    esSesionActual = esSesionActual,
    creadoEn = creadoEn,
    ultimaModificacion = ultimaModificacion
)

fun CategoriaEntity.toDomain(): Categoria = Categoria(
    id = id,
    nombre = nombre
)

fun Categoria.toEntity(): CategoriaEntity = CategoriaEntity(
    id = id,
    nombre = nombre
)

fun InsumoEntity.toDomain(): Insumo = Insumo(
    id = id,
    nombre = nombre,
    cantidadActual = cantidadActual,
    cantidadMinima = cantidadMinima,
    unidadMedida = unidadMedida,
    categoria = categoria ?: ""
)

fun Insumo.toEntity(sincronizado: Boolean = false): InsumoEntity = InsumoEntity(
    id = id,
    nombre = nombre,
    cantidadActual = cantidadActual,
    cantidadMinima = cantidadMinima,
    unidadMedida = unidadMedida,
    categoria = categoria,
    sincronizado = sincronizado
)

fun CajaSesionEntity.toDomain(): CajaSesion = CajaSesion(
    cajaId = cajaId,
    usuarioCajeroId = usuarioCajeroId,
    nombreCajero = nombreCajero,
    fechaApertura = fechaApertura,
    montoApertura = montoApertura,
    estado = estado
)

fun CajaSesion.toEntity(sincronizado: Boolean = false): CajaSesionEntity = CajaSesionEntity(
    cajaId = cajaId,
    usuarioCajeroId = usuarioCajeroId,
    nombreCajero = nombreCajero,
    fechaApertura = fechaApertura,
    montoApertura = montoApertura,
    estado = estado,
    sincronizado = sincronizado
)

fun CajaDetalleEntity.toDomain(): CajaDetalle = CajaDetalle(
    cajaId = cajaId,
    fechaCierre = fechaCierre ?: 0L,
    egresos = egresos,
    ingresosEfectivo = ingresosEfectivo,
    ingresosIzipay = ingresosIzipay,
    ingresosBilleteraDigital = ingresosBilleteraDigital,
    totalVentas = totalVentas,
    esperadoFisico = esperadoFisico,
    montoFisicoReal = montoFisicoReal,
    diferencia = diferencia,
    justificacion = justificacion
)

fun CajaDetalle.toEntity(): CajaDetalleEntity = CajaDetalleEntity(
    cajaId = cajaId,
    fechaCierre = fechaCierre,
    egresos = egresos,
    ingresosEfectivo = ingresosEfectivo,
    ingresosIzipay = ingresosIzipay,
    ingresosBilleteraDigital = ingresosBilleteraDigital,
    totalVentas = totalVentas,
    esperadoFisico = esperadoFisico,
    montoFisicoReal = montoFisicoReal,
    diferencia = diferencia,
    justificacion = justificacion
)

fun PedidoDetalleEntity.toDomain(): PedidoDetalle = PedidoDetalle(
    productoId = productoId,
    nombreProducto = nombreProducto,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    comentario = comentario
)

fun PedidoDetalle.toEntity(pedidoId: String): PedidoDetalleEntity = PedidoDetalleEntity(
    pedidoId = pedidoId,
    productoId = productoId,
    nombreProducto = nombreProducto,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    comentario = comentario
)

fun PedidoEntity.toDomain(detalles: List<PedidoDetalleEntity> = emptyList()): Pedido = Pedido(
    pedidoId = pedidoId,
    numeroPedido = numeroPedido,
    fecha = fecha,
    estado = estado,
    tipoPedido = tipoPedido,
    mesaId = mesaId,
    metodoPago = metodoPago,
    nombreCliente = nombreCliente,
    total = total,
    usuarioId = usuarioId,
    usuarioNombre = usuarioNombre,
    notas = notas,
    cajaId = cajaId,
    detalles = detalles.map { it.toDomain() }
)

fun Pedido.toEntity(sincronizado: Boolean = false): PedidoEntity = PedidoEntity(
    pedidoId = pedidoId,
    numeroPedido = numeroPedido,
    fecha = fecha,
    estado = estado,
    tipoPedido = tipoPedido,
    mesaId = mesaId,
    metodoPago = metodoPago,
    nombreCliente = nombreCliente,
    total = total,
    usuarioId = usuarioId,
    usuarioNombre = usuarioNombre,
    notas = notas,
    cajaId = cajaId,
    sincronizado = sincronizado
)

fun ProductoInsumoEntity.toDomain(): ProductoInsumo = ProductoInsumo(
    productoId = productoId,
    insumoId = insumoId,
    cantidadRequerida = cantidadRequerida
)

fun ProductoInsumo.toEntity(): ProductoInsumoEntity = ProductoInsumoEntity(
    productoId = productoId,
    insumoId = insumoId,
    cantidadRequerida = cantidadRequerida
)
