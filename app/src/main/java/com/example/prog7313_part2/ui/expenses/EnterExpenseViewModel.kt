package com.example.prog7313_part2.ui.expenses

import android.app.Application
import androidx.lifecycle.*
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import android.util.Log

class EnterExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository: ExpenseRepository
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()

    private val _categoryNames = MutableLiveData<List<String>>()
    val categoryNames: LiveData<List<String>> = _categoryNames

    private var currentUserId: Int = -1

    init {
        val expenseDao = AppDatabase.getDatabase(application).expenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
        loadCategories(currentUserId)
    }

    fun saveExpense(expense: Expense, userId: Int) {
        viewModelScope.launch {
            try {
                val expenseWithUser = expense.copy(userId = userId)
                expenseRepository.insertExpense(expenseWithUser)
                Log.d("EnterExpenseViewModel", "Expense saved successfully: $expenseWithUser")
            } catch (e: Exception) {
                Log.e("EnterExpenseViewModel", "Error saving expense: ${e.message}")
            }
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            try {
                val categories = categoryDao.getCategoriesForUser(userId)
                val names = categories.map { it.name }
                _categoryNames.postValue(names)
                Log.d("EnterExpenseViewModel", "Loaded user-specific categories: $names")
            } catch (e: Exception) {
                Log.e("EnterExpenseViewModel", "Failed to load user-specific categories: ${e.message}")
            }
        }
    }

}
