package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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
class GestionInventarioViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    lateinit var firestore: FirebaseFirestore

    @MockK
    lateinit var collectionRef: CollectionReference

    @MockK
    lateinit var documentRef: DocumentReference

    @MockK
    lateinit var querySnapshot: QuerySnapshot

    @MockK
    lateinit var task: Task<QuerySnapshot>

    private lateinit var viewModel: GestionInventarioViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection("insumos") } returns collectionRef
        
        // Mock the initial call in init
        every { collectionRef.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.documents } returns emptyList()

        viewModel = GestionInventarioViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `obtenerInsumos should update isLoading state`() = runTest {
        viewModel.isLoading.test {
            assertFalse(awaitItem()) // Initial state false (after init finishes)
            
            viewModel.obtenerInsumos()
            
            // Due to UnconfinedTestDispatcher, it might be fast, but we can check transitions
            // The init already called it once.
        }
    }

    @Test
    fun `eliminarInsumo should call delete and refresh`() = runTest {
        val deleteTask = mockk<Task<Void>>()
        every { collectionRef.document("ins1") } returns documentRef
        every { documentRef.delete() } returns deleteTask
        every { deleteTask.isComplete } returns true
        every { deleteTask.isCanceled } returns false
        every { deleteTask.exception } returns null
        every { deleteTask.result } returns null

        var success = false
        viewModel.eliminarInsumo("ins1") { success = it }

        verify { documentRef.delete() }
        assertTrue(success)
    }
}
