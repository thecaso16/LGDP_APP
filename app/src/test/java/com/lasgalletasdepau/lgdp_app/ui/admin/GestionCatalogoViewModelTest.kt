package com.lasgalletasdepau.lgdp_app.ui.admin

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.lasgalletasdepau.lgdp_app.data.local.AppDatabase
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.domain.model.Producto
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GestionCatalogoViewModelTest {

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
    lateinit var query: Query

    @MockK
    lateinit var task: Task<QuerySnapshot>

    @MockK
    lateinit var querySnapshot: QuerySnapshot

    private lateinit var viewModel: GestionCatalogoViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseFirestore::class)
        mockkObject(AppDatabase.Companion)

        every { application.applicationContext } returns application
        every { AppDatabase.getDatabase(any()) } returns appDatabase
        every { appDatabase.appDao() } returns appDao
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection(any()) } returns collectionRef
        
        // Mock Tasks to be successful
        every { task.isComplete } returns true
        every { task.isSuccessful } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        // Mock query chain
        every { collectionRef.whereEqualTo("activo", true) } returns query
        every { query.get() } returns task

        viewModel = GestionCatalogoViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `isLoading should be false after setup`() = runTest {
        viewModel.isLoading.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `guardarProducto should call firestore add when id is empty`() = runTest {
        val prod = Producto(id = "", nombre = "Nuevo")
        val addTask = mockk<Task<com.google.firebase.firestore.DocumentReference>>()
        
        every { collectionRef.add(any()) } returns addTask
        every { addTask.isComplete } returns true
        every { addTask.isSuccessful } returns true
        every { addTask.isCanceled } returns false
        every { addTask.exception } returns null
        every { addTask.result } returns mockk()
        
        var success = false
        viewModel.guardarProducto(prod) { success = it }
        
        verify { collectionRef.add(any()) }
        assertTrue(success)
    }
}
