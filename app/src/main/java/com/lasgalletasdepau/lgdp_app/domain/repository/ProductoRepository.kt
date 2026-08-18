package com.lasgalletasdepau.lgdp_app.domain.repository

import com.lasgalletasdepau.lgdp_app.domain.model.Categoria
import com.lasgalletasdepau.lgdp_app.domain.model.Insumo
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import com.lasgalletasdepau.lgdp_app.domain.model.ProductoInsumo
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    fun obtenerCategorias(): Flow<List<Categoria>>
    suspend fun agregarCategoria(nombre: String)
    fun obtenerProductos(): Flow<List<Producto>>
    suspend fun guardarProducto(producto: Producto)
    suspend fun eliminarProductoLogico(productoId: String)
    fun obtenerInsumos(): Flow<List<Insumo>>
    suspend fun obtenerInsumosPorProducto(productoId: String): List<ProductoInsumo>
    suspend fun guardarVinculoInsumo(productoId: String, insumoId: String, cantidad: Double)
    suspend fun eliminarVinculoInsumo(productoId: String, insumoId: String)
    suspend fun guardarInsumo(insumo: Insumo)
    suspend fun eliminarInsumo(insumoId: String)
    suspend fun sincronizarCatalogo()
}
