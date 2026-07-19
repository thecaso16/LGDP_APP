package com.lasgalletasdepau.lgdp_app.ui.admin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportesNegocioViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    lateinit var firestore: FirebaseFirestore

    @MockK
    lateinit var collectionRef: CollectionReference

    @MockK
    lateinit var querySnapshot: QuerySnapshot

    @MockK
    lateinit var task: Task<QuerySnapshot>

    private lateinit var viewModel: ReportesNegocioViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns firestore
        every { firestore.collection(any()) } returns collectionRef
        
        viewModel = ReportesNegocioViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state should be zero`() = runTest {
        assertEquals(0.0, viewModel.totalIngresos.value, 0.0)
        assertEquals(0, viewModel.totalPedidos.value)
    }

    @Test
    fun `isLoading should be false initially`() = runTest {
        assertFalse(viewModel.isLoading.value)
    }
}
