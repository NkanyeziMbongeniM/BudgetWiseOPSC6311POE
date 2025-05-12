package com.example.prog7313_part2.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Update
import com.example.prog7313_part2.data.local.entities.Expense

@Dao
interface ExpenseDao {
    // Insert or replace an existing expense
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    // Get all expenses, ordered by date descending
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesByUserId(userId: Int): List<Expense>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesByUserIdLiveData(userId: Int): LiveData<List<Expense>>

    // Get count of all expenses as a suspend function (simpler)
    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int

    // Get expense by ID
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Update
    suspend fun updateExpense(expense: Expense)

    // Delete an expense by ID
    @Delete
    suspend fun deleteExpense(expense: Expense)
}
