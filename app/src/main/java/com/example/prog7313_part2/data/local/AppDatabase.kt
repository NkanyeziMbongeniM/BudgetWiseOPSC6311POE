package com.example.prog7313_part2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.example.prog7313_part2.data.local.dao.BudgetGoalsDao
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.data.local.entities.User
import com.example.prog7313_part2.data.local.entities.BudgetGoals
import com.example.prog7313_part2.data.local.dao.CategoryDao
import com.example.prog7313_part2.data.local.dao.ExpenseDao
import com.example.prog7313_part2.data.local.dao.UserDao

@Database(entities = [User::class, Category::class, Expense::class, BudgetGoals::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalsDao(): BudgetGoalsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "my_database"
                            ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}