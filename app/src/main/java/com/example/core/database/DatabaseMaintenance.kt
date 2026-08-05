package com.example.core.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.core.utils.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseMaintenance {

    suspend fun performDatabaseOptimization(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val sqliteDb: SupportSQLiteDatabase = db.openHelper.writableDatabase
                
                AppLogger.i("DatabaseMaintenance", "Running SQLite WAL checkpoint & maintenance...")
                
                // SQLite PRAGMAs & Optimizations
                sqliteDb.execSQL("PRAGMA wal_checkpoint(FULL);")
                sqliteDb.execSQL("ANALYZE;")
                sqliteDb.execSQL("VACUUM;")
                
                AppLogger.i("DatabaseMaintenance", "Database optimization completed successfully.")
                true
            } catch (e: Exception) {
                AppLogger.e("DatabaseMaintenance", "Error optimizing database: ${e.message}", e)
                false
            }
        }
    }
}
