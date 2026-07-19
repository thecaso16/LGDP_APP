package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.InsumoEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.ProductoInsumoEntity
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PedidoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    lateinit var application: Application

    @MockK
    lateinit var appDao: AppDao

    @MockK
    lateinit var appDatabase: AppDatabase

    @MockK
    lateinit var syncManager: SyncManager

    private lateinit var viewModel: PedidoViewModel

    // Use a MutableStateFlow for the logged user to simulate changes
    private val userFlow = MutableStateFlow<UsuarioEntity?>(null)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(AppDatabase::class)
        mockkObject(AppDatabase.Companion) // CRITICAL
        mockkStatic(SyncManager::class)
        mockkObject(SyncManager.Companion) // CRITICAL

        every { application.applicationContext } returns application
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao
        every { SyncManager.getInstance(any()) } returns syncManager

        // IMPORTANT: Prevent OOM by making the infinite sync loop wait
        coEvery { syncManager.sincronizarPedidosYEstado() } coAnswers { awaitCancellation() }

        // Flow mocks for init
        every { appDao.obtenerUsuarioLogueado() } returns userFlow
        every { appDao.obtenerCategorias() } returns flowOf(emptyList())
        every { appDao.obtenerProductos() } returns flowOf(emptyList())

        viewModel = PedidoViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `agregarProducto should update carrito and handle stock correctly`() = runTest {
        val producto = ProductoEntity(
            productoId = "p1",
            nombre = "Galleta",
            descripcion = "",
            imagen = null,
            precio = 5.0,
            stock = 10,
            controlaStock = true,
            categoriaId = "cat1",
            recomendado = false,
            estaDisponible = true,
            activo = true,
            sincronizado = true,
            ultimaActualizacion = 0L,
            operacionPendiente = null
        )

        viewModel.agregarProducto(producto)

        viewModel.carrito.test {
            val currentCarrito = awaitItem()
            assertTrue(currentCarrito.containsKey("p1"))
            assertEquals(1, currentCarrito["p1"]?.cantidad)
            assertEquals(5.0, currentCarrito["p1"]?.precioUnitario ?: 0.0, 0.0)
        }
    }

    @Test
    fun `agregarProducto should emit error when stock is insufficient`() = runTest {
        val producto = ProductoEntity(
            productoId = "p1",
            nombre = "Galleta",
            descripcion = "",
            imagen = null,
            precio = 5.0,
            stock = 0,
            controlaStock = true,
            categoriaId = "cat1",
            recomendado = false,
            estaDisponible = true,
            activo = true,
            sincronizado = true,
            ultimaActualizacion = 0L,
            operacionPendiente = null
        )

        viewModel.errorEvent.test {
            viewModel.agregarProducto(producto)
            assertEquals("Stock insuficiente de Galleta", awaitItem())
        }
    }

    @Test
    fun `agregarProducto should validate insumos when controlaStock is false`() = runTest {
        val producto = ProductoEntity(
            productoId = "p1",
            nombre = "Galleta Especial",
            descripcion = "",
            imagen = null,
            precio = 10.0,
            stock = 100,
            controlaStock = false,
            categoriaId = "cat1",
            recomendado = false,
            estaDisponible = true,
            activo = true,
            sincronizado = true,
            ultimaActualizacion = 0L,
            operacionPendiente = null
        )
        
        val insumoRequerido = ProductoInsumoEntity("p1", "ins1", 0.5)
        val insumoEstado = InsumoEntity("ins1", "Harina", 0.1, 1.0, "kg", "Abarrotes")

        coEvery { appDao.obtenerInsumosPorProducto("p1") } returns listOf(insumoRequerido)
        every { appDao.obtenerInsumos() } returns flowOf(listOf(insumoEstado))

        viewModel.errorEvent.test {
            viewModel.agregarProducto(producto)
            assertEquals("Falta insumo: Harina", awaitItem())
        }
    }

    @Test
    fun `quitarProducto should reduce quantity or remove item`() = runTest {
        val producto = ProductoEntity(
            productoId = "p1",
            nombre = "Galleta",
            descripcion = "",
            imagen = null,
            precio = 5.0,
            stock = 10,
            controlaStock = true,
            categoriaId = "cat1",
            recomendado = false,
            estaDisponible = true,
            activo = true,
            sincronizado = true,
            ultimaActualizacion = 0L,
            operacionPendiente = null
        )
        
        viewModel.agregarProducto(producto)
        viewModel.agregarProducto(producto)
        
        viewModel.carrito.test {
            assertEquals(2, awaitItem()["p1"]?.cantidad)
            
            viewModel.quitarProducto("p1")
            assertEquals(1, awaitItem()["p1"]?.cantidad)
            
            viewModel.quitarProducto("p1")
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun `limpiarCarrito should reset all state`() = runTest {
        val producto = ProductoEntity(
            productoId = "p1",
            nombre = "Galleta",
            descripcion = "",
            imagen = null,
            precio = 5.0,
            stock = 10,
            controlaStock = true,
            categoriaId = "cat1",
            recomendado = false,
            estaDisponible = true,
            activo = true,
            sincronizado = true,
            ultimaActualizacion = 0L,
            operacionPendiente = null
        )
        viewModel.agregarProducto(producto)
        viewModel.actualizarNotasGlobales("Nota")

        viewModel.limpiarCarrito()

        assertTrue(viewModel.carrito.value.isEmpty())
        assertEquals("", viewModel.notasGlobales.value)
    }
}
