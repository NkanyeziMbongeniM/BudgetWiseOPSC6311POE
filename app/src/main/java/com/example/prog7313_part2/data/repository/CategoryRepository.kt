package com.example.prog7313_part2.data.local.repository

import com.example.prog7313_part2.data.local.dao.CategoryDao
import com.example.prog7313_part2.data.local.entities.Category

class CategoryRepository(private val categoryDao: CategoryDao) {
    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    suspend fun getCategories(userId: Int): List<Category> {
        return categoryDao.getCategoriesForUser(userId)
    }

    suspend fun getCategoryById(categoryId: Long): Category? {
        return categoryDao.getCategoryById(categoryId)
    }

    suspend fun deleteCategory(categoryId: Long) {
        categoryDao.deleteCategory(categoryId)
    }

    suspend fun getAllCategoryNames(): List<String> {
        return categoryDao.getAllCategoryNames()
    }
}