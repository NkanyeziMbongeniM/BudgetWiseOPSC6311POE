package com.example.prog7313_part2.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.prog7313_part2.data.local.entities.Category
import com.example.prog7313_part2.data.local.entities.Expense

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategoriesForUser(userId: Int): List<Category>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: Long)

    @Query("SELECT name FROM categories")
    suspend fun getAllCategoryNames(): List<String>

    @Query("SELECT * FROM categories")
    fun getAllLiveCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>
}