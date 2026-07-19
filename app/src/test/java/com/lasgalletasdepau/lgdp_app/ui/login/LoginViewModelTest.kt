package com.lasgalletasdepau.lgdp_app.ui.login

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    lateinit var application: Application

    @MockK
    lateinit var auth: FirebaseAuth

    @MockK
    lateinit var firestore: FirebaseFirestore

    @MockK
    lateinit var appDao: AppDao

    @MockK
    lateinit var appDatabase: AppDatabase

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Mock statics and objects
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseFirestore::class)
        mockkObject(AppDatabase.Companion)

        // Provide applicationContext mock
        every { application.applicationContext } returns application
        every { application.resources } returns mockk(relaxed = true)

        every { FirebaseAuth.getInstance() } returns auth
        every { FirebaseFirestore.getInstance() } returns firestore
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao

        // Default behavior for init
        every { auth.currentUser } returns null

        viewModel = LoginViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `iniciarSesion with empty fields should emit Error`() = runTest {
        viewModel.loginState.test {
            // Initial state from init (verificarSesionExistente calls auth.currentUser which is null)
            assertEquals(LoginState.Idle, awaitItem())

            viewModel.iniciarSesion("", "")
            
            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertEquals("Por favor, completa todos los campos.", (errorState as LoginState.Error).mensaje)
        }
    }

    @Test
    fun `cerrarSesion should reset state and call auth signOut`() = runTest {
        every { auth.signOut() } just Runs
        coEvery { appDao.cerrarSesionLocal() } just Runs

        viewModel.cerrarSesion()

        verify { auth.signOut() }
        coVerify { appDao.cerrarSesionLocal() }
        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }

    @Test
    fun `resetearEstado should set state to Idle`() {
        // Force a different state first if needed, but here we just check the call
        viewModel.resetearEstado()
        assertEquals(LoginState.Idle, viewModel.loginState.value)
    }
}
