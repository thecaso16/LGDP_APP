package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportesTrabajadoresViewModelTest {

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

    private lateinit var viewModel: ReportesTrabajadoresViewModel
    
    private val userFlow = MutableStateFlow<UsuarioEntity?>(null)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(AppDatabase::class)
        mockkObject(AppDatabase.Companion)
        mockkStatic(SyncManager::class)
        mockkObject(SyncManager.Companion)

        every { application.applicationContext } returns application
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao
        every { SyncManager.getInstance(any()) } returns syncManager

        every { appDao.obtenerUsuarioLogueado() } returns userFlow

        viewModel = ReportesTrabajadoresViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `esCajeroOAdmin should be true for CAJERO role`() = runTest {
        val cajero = UsuarioEntity("u1", "c@t.com", "C", "T", "1", "Cajero", true)
        userFlow.value = cajero
        
        viewModel.usuarioLogueado.test {
            assertEquals(cajero, awaitItem())
            assertTrue(viewModel.esCajeroOAdmin())
        }
    }

    @Test
    fun `esCajeroOAdmin should be false for TRABAJADOR role`() = runTest {
        val trab = UsuarioEntity("u1", "t@t.com", "T", "T", "1", "Trabajador", true)
        userFlow.value = trab
        
        viewModel.usuarioLogueado.test {
            assertEquals(trab, awaitItem())
            assertFalse(viewModel.esCajeroOAdmin())
        }
    }

    @Test
    fun `cambiarModo should update state`() {
        viewModel.cambiarModo(ModoHistorial.BUSQUEDA_HISTORICA)
        assertEquals(ModoHistorial.BUSQUEDA_HISTORICA, viewModel.modo.value)
    }

    @Test
    fun `esCajeroResponsable should return true if UIDs match`() = runTest {
        val user = UsuarioEntity("uid_123", "t@t.com", "T", "T", "1", "Trabajador", true)
        userFlow.value = user
        
        viewModel.usuarioLogueado.test {
            assertEquals(user, awaitItem())
            assertTrue(viewModel.esCajeroResponsable("uid_123"))
            assertFalse(viewModel.esCajeroResponsable("uid_456"))
        }
    }
}
