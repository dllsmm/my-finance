package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FinanceDatabase
import com.example.data.model.CustomCategory
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // Main Flows
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CustomCategory>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived Metrics
    val totalIncome: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == "INCOME" }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalExpense: StateFlow<Double> = transactions
        .map { list -> list.filter { it.type == "EXPENSE" }.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalBalance: StateFlow<Double> = transactions
        .map { list ->
            val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            income - expense
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Category Wise Chart Data
    val expenseByCategory: StateFlow<Map<String, Double>> = transactions
        .map { list ->
            list.filter { it.type == "EXPENSE" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val incomeByCategory: StateFlow<Map<String, Double>> = transactions
        .map { list ->
            list.filter { it.type == "INCOME" }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Database Actions
    fun addTransaction(amount: Double, type: String, category: String, description: String, date: Long) {
        viewModelScope.launch {
            val transaction = Transaction(
                amount = amount,
                type = type,
                category = category,
                description = description,
                date = date
            )
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: String, iconName: String) {
        viewModelScope.launch {
            val category = CustomCategory(
                name = name,
                type = type,
                iconName = iconName
            )
            repository.insertCategory(category)
        }
    }

    fun deleteCategory(category: CustomCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Factory Class
    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = FinanceDatabase.getDatabase(application, CoroutineScope(Dispatchers.IO))
                    val repository = FinanceRepository(database.transactionDao(), database.categoryDao())
                    return FinanceViewModel(application, repository) as T
                }
            }
        }
    }
}
