package com.example.prog7313_part2.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.data.local.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CategoryRepository
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories
    private val _category = MutableLiveData<Category?>()
    val category: LiveData<Category?> get() = _category

    init {
        val categoryDao = AppDatabase.getDatabase(application).categoryDao()
        repository = CategoryRepository(categoryDao)
    }

    fun insertCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun loadCategories(userId: Int) {
        viewModelScope.launch {
            val categoryList = repository.getCategories(userId)
            _categories.postValue(categoryList)
        }
    }

    fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = repository.getCategoryById(categoryId)
            _category.postValue(category)
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }
}