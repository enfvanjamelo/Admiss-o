package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface AdmissionDao {
    @Query("SELECT * FROM admissions ORDER BY timestamp DESC")
    fun getAllAdmissions(): Flow<List<AdmissionRecord>>

    @Query("SELECT * FROM admissions WHERE id = :id LIMIT 1")
    suspend fun getAdmissionById(id: Long): AdmissionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmission(record: AdmissionRecord): Long

    @Delete
    suspend fun deleteAdmission(record: AdmissionRecord)

    @Query("DELETE FROM admissions WHERE id = :id")
    suspend fun deleteAdmissionById(id: Long)
}

@Database(entities = [AdmissionRecord::class], version = 5, exportSchema = false)
abstract class AdmissionDatabase : RoomDatabase() {
    abstract fun admissionDao(): AdmissionDao

    companion object {
        @Volatile
        private var INSTANCE: AdmissionDatabase? = null

        fun getDatabase(context: Context): AdmissionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AdmissionDatabase::class.java,
                    "admission_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
