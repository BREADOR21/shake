package com.example.data

import kotlinx.coroutines.flow.Flow

class SpendingRepository(private val spendingDao: SpendingDao) {
    val allSpendings: Flow<List<SpendingEntry>> = spendingDao.getAllSpendings()

    suspend fun insert(spending: SpendingEntry) {
        spendingDao.insertSpending(spending)
    }

    suspend fun deleteById(id: Int) {
        spendingDao.deleteSpendingById(id)
    }
}
