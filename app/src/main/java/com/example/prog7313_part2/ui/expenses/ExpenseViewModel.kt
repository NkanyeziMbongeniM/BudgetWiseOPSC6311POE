package com.example.prog7313_part2.ui.expenses

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.Expense
import com.example.prog7313_part2.data.repository.ExpenseRepository
 import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseRepository = ExpenseRepository(AppDatabase.getDatabase(application).expenseDao())
    private val categoryDao = AppDatabase.getDatabase(application).categoryDao()
    val categoryNames: LiveData<List<String>> get() = _categoryNames
    private val _categoryNames = MutableLiveData<List<String>>()

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.update(expense) // Implement this in your repository
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
