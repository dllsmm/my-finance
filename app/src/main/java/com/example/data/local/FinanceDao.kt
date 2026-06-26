package com.example.data.local

import androidx.room.*
import com.example.data.model.CustomCategory
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)

    @Delete
    suspend fun deleteCategory(category: CustomCategory)
}
