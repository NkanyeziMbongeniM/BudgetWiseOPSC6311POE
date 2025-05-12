package com.example.prog7313_part2.ui.budget

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.BudgetGoals
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.data.repository.BudgetRepository
import com.example.prog7313_part2.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BudgetViewModel : ViewModel() {

    private lateinit var budgetRepository: BudgetRepository
    private lateinit var expenseRepository: ExpenseRepository
    private val _userId = MutableLiveData<Int>()
    private val currentMonth: String

    init {
        // Initialize current month in MM-yyyy format
        val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
        currentMonth = dateFormat.format(Date()).replace("-", "")
    }

    // Initialization method to set up repositories with the database context
    fun initialize(context: android.content.Context) {
        val db = AppDatabase.getDatabase(context)
        budgetRepository = BudgetRepository(db.budgetGoalsDao())
        expenseRepository = ExpenseRepository(db.expenseDao())
    }

    // LiveData for budget goals based on user ID
    val budgetGoals: LiveData<BudgetGoals?> = _userId.switchMap { userId ->
        budgetRepository.getCurrentBudgetGoals(userId)
    }

    // LiveData for all expenses based on user ID
    val expenses: LiveData<List<Expense>> = _userId.switchMap { userId ->
        expenseRepository.getExpensesByUserIdLiveData(userId)
    }

    // LiveData for filtering expenses in the current month
    private val monthlyExpenses: LiveData<List<Expense>> = expenses.map { allExpenses ->
        allExpenses.filter {
            it.date.substring(0, 7).replace("-", "") == currentMonth
        }
    }

    // Calculate total spending for the current month
    val totalSpending: LiveData<Double> = monthlyExpenses.map { monthly ->
        monthly.sumOf { it.amount }
    }

    // Calculate category totals for the current month
    val categoryTotals: LiveData<List<Pair<String, Double>>> = monthlyExpenses.map { monthly ->
        monthly.groupBy { it.category }
            .map { (category, expenses) -> Pair(category, expenses.sumOf { it.amount }) }
            .sortedBy { it.first }
    }

    // Load budget data based on user ID
    fun loadBudgetData(userId: Int) {
        _userId.value = userId
    }

    // Set budget goals for a user
    fun setBudgetGoals(min: Double, max: Double) {
        viewModelScope.launch {
            val userId = _userId.value ?: return@launch
            val dateFormat = SimpleDateFormat("MM-yyyy", Locale.getDefault())
            val monthYear = dateFormat.format(Date())
            val goals = BudgetGoals(
                userId = userId,
                monthYear = monthYear,
                minMonthlyGoal = min,
                maxMonthlyGoal = max
            )
            budgetRepository.insertBudgetGoals(goals)
        }
    }

}