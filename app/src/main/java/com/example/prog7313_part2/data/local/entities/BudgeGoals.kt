package com.example.prog7313_part2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoals(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Updated to Int from Long
    val userId: Int,
    val monthYear: String, // e.g., "05-2025"
    val minMonthlyGoal: Double,
    val maxMonthlyGoal: Double
)