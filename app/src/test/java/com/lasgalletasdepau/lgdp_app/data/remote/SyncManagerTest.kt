package com.lasgalletasdepau.lgdp_app.data.remote

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    @MockK
    lateinit var appDao: AppDao

    @MockK
    lateinit var firestore: FirebaseFirestore

    private lateinit var syncManager: SyncManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        
        syncManager = SyncManager(appDao, firestore)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `sincronizarTodo should call dao methods`() = runTest {
        coEvery { appDao.obtenerProductosNoSincronizados() } returns emptyList()
        coEvery { appDao.obtenerMesasNoSincronizadas() } returns emptyList()
        coEvery { appDao.obtenerPedidosPendientesDeSincronizar() } returns emptyList()
        
        // Mocking firestore get calls would be complex, let's just check the upload part
        // which returns early if empty
        
        syncManager.sincronizarTodo()
        
        coVerify { appDao.obtenerProductosNoSincronizados() }
        coVerify { appDao.obtenerMesasNoSincronizadas() }
        coVerify { appDao.obtenerPedidosPendientesDeSincronizar() }
    }
}
