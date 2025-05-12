package com.example.prog7313_part2.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.prog7313_part2.data.local.entities.BudgetGoals

@Dao
interface BudgetGoalsDao {

    // Insert or update budget goals using REPLACE strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budgetGoals: BudgetGoals)

    // Retain original insert method if needed elsewhere
    @Insert
    suspend fun insertBudgetGoals(goals: BudgetGoals)

    // Get current goals using LiveData (for observing in UI)
    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY monthYear DESC LIMIT 1")
    fun getCurrentBudgetGoals(userId: Int): LiveData<BudgetGoals?>

    // Get budget goals for specific month
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND monthYear = :monthYear")
    suspend fun getBudgetGoalsForMonth(userId: Int, monthYear: String): BudgetGoals?

    // Get latest budget goals as suspend
    @Query("SELECT * FROM budget_goals WHERE userId = :userId ORDER BY monthYear DESC LIMIT 1")
    suspend fun getLatestBudgetGoals(userId: Int): BudgetGoals?
}