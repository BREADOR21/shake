package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendingDao {
    @Query("SELECT * FROM spendings ORDER BY timestamp DESC")
    fun getAllSpendings(): Flow<List<SpendingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpending(spending: SpendingEntry)

    @Query("DELETE FROM spendings WHERE id = :id")
    suspend fun deleteSpendingById(id: Int)
}
