package com.example.prog7313_part2.ui.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.data.local.entities.Expense
import kotlinx.coroutines.launch
import android.util.Log
import com.example.prog7313_part2.data.repository.ExpenseRepository

class ViewAllExpensesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseDao = db.expenseDao()
    private val categoryDao = db.categoryDao()

    private val expenseRepository = ExpenseRepository(expenseDao)

    private val _userExpenses = MutableLiveData<List<Expense>>()
    val userExpenses: LiveData<List<Expense>> = _userExpenses

    private val _userCategories = MutableLiveData<List<Category>>()
    val userCategories: LiveData<List<Category>> = _userCategories

    private var currentUserId: Int = -1

    fun loadExpenses(userId: Int) {
        currentUserId = userId
        viewModelScope.launch {
            try {
                val expenses = expenseRepository.getExpensesByUserId(userId)
                _userExpenses.postValue(expenses)
                Log.d("ViewAllExpensesVM", "Loaded ${expenses.size} expenses for userId=$userId")
            } catch (e: Exception) {
                Log.e("ViewAllExpensesVM", "Error loading expenses: ${e.message}")
                _userExpenses.postValue(emptyList())
            }
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            try {
                val categories = categoryDao.getCategoriesForUser(userId)
                _userCategories.postValue(categories)
                Log.d("ViewAllExpensesVM", "Loaded ${categories.size} categories for userId=$userId")
            } catch (e: Exception) {
                Log.e("ViewAllExpensesVM", "Error loading categories: ${e.message}")
                _userCategories.postValue(emptyList())
            }
        }
    }

}
