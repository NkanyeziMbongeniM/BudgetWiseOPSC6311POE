package com.example.prog7313_part2.data.repository

import androidx.lifecycle.LiveData
import com.example.prog7313_part2.data.local.dao.ExpenseDao
import com.example.prog7313_part2.data.local.entities.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepository(private val dao: ExpenseDao)
{
    suspend fun insertExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            dao.insertExpense(expense)
        }
    }

    fun getAll(): LiveData<List<Expense>> = dao.getAllExpenses()

    fun getExpensesByUserIdLiveData(userId: Int): LiveData<List<Expense>> {
        return dao.getExpensesByUserIdLiveData(userId)
    }

    suspend fun getExpenseById(expenseId: Int): Expense? {
        return dao.getExpenseById(expenseId)
    }

    suspend fun update(expense: Expense) {
        dao.updateExpense(expense)
    }

    suspend fun getExpensesByUserId(userId: Int): List<Expense> {
        return withContext(Dispatchers.IO) {
            dao.getExpensesByUserId(userId)
        }
    }

    suspend fun delete(expense: Expense) {
        dao.deleteExpense(expense)
    }
}
