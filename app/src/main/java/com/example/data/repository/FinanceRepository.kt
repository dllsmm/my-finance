package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.model.CustomCategory
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<CustomCategory>> = categoryDao.getAllCategories()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertCategory(category: CustomCategory) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CustomCategory) {
        categoryDao.deleteCategory(category)
    }
}
