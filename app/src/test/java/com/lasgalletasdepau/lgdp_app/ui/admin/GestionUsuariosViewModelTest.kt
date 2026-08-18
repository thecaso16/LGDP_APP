package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.domain.model.Usuario
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GestionUsuariosViewModelTest {

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
    lateinit var firestore: FirebaseFirestore

    @MockK
    lateinit var collectionRef: CollectionReference

    @MockK
    lateinit var task: Task<QuerySnapshot>

    @MockK
    lateinit var querySnapshot: QuerySnapshot

    private lateinit var viewModel: GestionUsuariosViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseFirestore::class)
        mockkObject(AppDatabase.Companion)
        mockkStatic(FirebaseApp::class)
        mockkStatic(FirebaseAuth::class)

        every { application.applicationContext } returns application
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("usuarios") } returns collectionRef
        
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        // Mock init call
        every { collectionRef.get() } returns task

        viewModel = GestionUsuariosViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `isLoading should be false after init`() = runTest {
        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `crearNuevoUsuarioConAuth with invalid data should set error`() = runTest {
        val user = Usuario(email = "")
        var result = true
        
        viewModel.crearNuevoUsuarioConAuth(user, "123", emptyList()) { result = it }
        
        assertFalse(result)
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
        }
    }
}
