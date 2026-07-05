package com.lasgalletasdepau.lgdp_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lasgalletasdepau.lgdp_app.data.local.dao.AppDao
import com.lasgalletasdepau.lgdp_app.data.local.entity.*

@Database(
    entities = [
        UsuarioEntity::class,
        CategoriaEntity::class,
        ProductoEntity::class,
        PedidoEntity::class,
        PedidoDetalleEntity::class,
        MesaEntity::class,
        CajaSesionEntity::class
    ],
    version = 8, // Incrementado por cambios de esquema pendientes y nuevas funcionalidades de caja
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "galletas_pau_offline_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}