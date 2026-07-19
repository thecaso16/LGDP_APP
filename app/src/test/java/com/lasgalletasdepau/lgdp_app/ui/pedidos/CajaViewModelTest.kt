package com.lasgalletasdepau.lgdp_app.ui.pedidos

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.UsuarioEntity
import com.lasgalletasdepau.lgdp_app.domain.model.RolUsuario
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
class CajaViewModelTest {

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
    lateinit var auth: FirebaseAuth

    @MockK
    lateinit var firestore: FirebaseFirestore

    private lateinit var viewModel: CajaViewModel

    // User flow to control reactive states
    private val userFlow = MutableStateFlow<UsuarioEntity?>(null)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)
        mockkStatic(AppDatabase::class)
        mockkObject(AppDatabase.Companion)

        every { application.applicationContext } returns application
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao
        every { FirebaseAuth.getInstance() } returns auth
        every { FirebaseFirestore.getInstance() } returns firestore

        // Default behavior
        every { auth.currentUser } returns null
        every { appDao.obtenerUsuarioLogueado() } returns userFlow
        every { appDao.obtenerCajaAbierta() } returns flowOf(null)

        viewModel = CajaViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `esCajero should be true for ADMINISTRADOR`() = runTest {
        val admin = UsuarioEntity("u1", "admin@test.com", "Admin", "Test", "123", "Administrador", true)
        userFlow.value = admin
        
        viewModel.esCajero.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `esCajero should be true for CAJERO`() = runTest {
        val cajero = UsuarioEntity("u2", "cajero@test.com", "Cajero", "Test", "456", "Cajero", true)
        userFlow.value = cajero

        viewModel.esCajero.test {
            assertTrue(awaitItem())
        }
    }

    @Test
    fun `esCajero should be false for TRABAJADOR`() = runTest {
        val trabajador = UsuarioEntity("u3", "trab@test.com", "Trab", "Test", "789", "Trabajador", true)
        userFlow.value = trabajador

        viewModel.esCajero.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `tieneRolCajero should return true for CAJERO and ADMIN roles`() = runTest {
        val cajero = UsuarioEntity("u2", "cajero@test.com", "Cajero", "Test", "456", "Cajero", true)
        userFlow.value = cajero
        
        // Wait for stateIn to update
        viewModel.usuarioLogueado.test {
            assertEquals(cajero, awaitItem())
            assertTrue(viewModel.tieneRolCajero())
        }

        val admin = UsuarioEntity("u1", "admin@test.com", "Admin", "Test", "123", "Administrador", true)
        userFlow.value = admin
        viewModel.usuarioLogueado.test {
            assertEquals(admin, awaitItem())
            assertTrue(viewModel.tieneRolCajero())
        }

        val trab = UsuarioEntity("u3", "t@t.com", "T", "T", "1", "Trabajador", true)
        userFlow.value = trab
        viewModel.usuarioLogueado.test {
            assertEquals(trab, awaitItem())
            assertFalse(viewModel.tieneRolCajero())
        }
    }

    @Test
    fun `abrirCaja should handle empty monto correctly`() {
        viewModel.montoApertura.value = ""
        // No crash expected
        viewModel.abrirCaja()
    }
}
