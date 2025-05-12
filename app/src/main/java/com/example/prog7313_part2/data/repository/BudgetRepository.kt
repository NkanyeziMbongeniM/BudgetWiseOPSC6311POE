package com.example.prog7313_part2.data.repository

import androidx.lifecycle.LiveData
import com.example.prog7313_part2.data.local.dao.BudgetGoalsDao
import com.example.prog7313_part2.data.local.entities.BudgetGoals
import java.text.SimpleDateFormat
import java.util.*

class BudgetRepository(private val budgetGoalsDao: BudgetGoalsDao) {
    private val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())

    // Existing method using LiveData (for observing in UI)
    fun getCurrentBudgetGoals(userId: Int): LiveData<BudgetGoals?> {
        return budgetGoalsDao.getCurrentBudgetGoals(userId)
    }

    // Newer suspend method for getting current budget goals
    suspend fun getCurrentBudgetGoalsSuspend(userId: Int): BudgetGoals? {
        val currentMonthYear = dateFormat.format(Date())
        return budgetGoalsDao.getBudgetGoalsForMonth(userId, currentMonthYear)
    }

    suspend fun getLatestBudgetGoals(userId: Int): BudgetGoals? {
        return budgetGoalsDao.getLatestBudgetGoals(userId)
    }

    suspend fun setBudgetGoals(userId: Int, min: Double, max: Double): Boolean {
        return try {
            val currentMonthYear = dateFormat.format(Date())
            val goals = BudgetGoals(
                userId = userId,
                monthYear = currentMonthYear,
                minMonthlyGoal = min,
                maxMonthlyGoal = max
            )
            budgetGoalsDao.insertOrUpdate(goals)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Method to insert goals directly
    suspend fun insertBudgetGoals(goals: BudgetGoals) {
        budgetGoalsDao.insertBudgetGoals(goals)
    }
}