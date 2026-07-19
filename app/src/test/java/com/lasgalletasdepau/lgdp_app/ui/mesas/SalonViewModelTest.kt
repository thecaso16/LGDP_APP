package com.lasgalletasdepau.lgdp_app.ui.mesas

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.MesaEntity
import com.lasgalletasdepau.lgdp_app.domain.model.EstadoMesa
import com.lasgalletasdepau.lgdp_app.data.remote.SyncManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SalonViewModelTest {

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

    private lateinit var viewModel: SalonViewModel

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
        
        coEvery { syncManager.sincronizarTodo() } just Runs

        every { appDao.obtenerEstadoMesas() } returns flowOf(emptyList())
        every { appDao.obtenerCajaAbierta() } returns flowOf(null)

        viewModel = SalonViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `mesas flow should emit values from DAO`() = runTest {
        val mesasMock = listOf(
            MesaEntity(1, "Mesa 01", EstadoMesa.LIBRE, null, true),
            MesaEntity(2, "Mesa 02", EstadoMesa.OCUPADA, "Juan", true)
        )
        every { appDao.obtenerEstadoMesas() } returns flowOf(mesasMock)
        
        // Re-init to pick up the new flow for this test
        val vm = SalonViewModel(application)
        
        vm.mesas.test {
            assertEquals(mesasMock, awaitItem())
        }
    }

    @Test
    fun `abrirMesa should call DAO and sync`() = runTest {
        coEvery { appDao.marcarMesaOcupada(1, "Carlos") } just Runs
        
        viewModel.abrirMesa(1, "Carlos")
        
        coVerify { appDao.marcarMesaOcupada(1, "Carlos") }
        coVerify { syncManager.sincronizarTodo() }
    }

    @Test
    fun `forzarLimpiezaMesa should call DAO and sync`() = runTest {
        coEvery { appDao.liberarMesa(1) } just Runs
        
        viewModel.forzarLimpiezaMesa(1)
        
        coVerify { appDao.liberarMesa(1) }
        coVerify { syncManager.sincronizarTodo() }
    }
}
