package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CustomCategory
import com.example.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class, CustomCategory::class], version = 1, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_tracker_db"
                )
                .addCallback(FinanceDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class FinanceDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val categoryDao = database.categoryDao()
                    
                    // Seed initial premium categories
                    val defaultCategories = listOf(
                        // Income categories
                        CustomCategory(name = "Salary", type = "INCOME", iconName = "AccountBalance"),
                        CustomCategory(name = "Investments", type = "INCOME", iconName = "ShowChart"),
                        CustomCategory(name = "Freelance", type = "INCOME", iconName = "LaptopMac"),
                        CustomCategory(name = "Gifts", type = "INCOME", iconName = "Redeem"),
                        
                        // Expense categories
                        CustomCategory(name = "Dining", type = "EXPENSE", iconName = "Restaurant"),
                        CustomCategory(name = "Shopping", type = "EXPENSE", iconName = "LocalMall"),
                        CustomCategory(name = "Transport", type = "EXPENSE", iconName = "DirectionsCar"),
                        CustomCategory(name = "Groceries", type = "EXPENSE", iconName = "ShoppingCart"),
                        CustomCategory(name = "Entertainment", type = "EXPENSE", iconName = "SportsEsports"),
                        CustomCategory(name = "Bills & Rent", type = "EXPENSE", iconName = "HomeWork"),
                        CustomCategory(name = "Health", type = "EXPENSE", iconName = "MedicalInformation"),
                        CustomCategory(name = "Others", type = "EXPENSE", iconName = "Payments")
                    )
                    
                    for (category in defaultCategories) {
                        categoryDao.insertCategory(category)
                    }
                }
            }
        }
    }
}
