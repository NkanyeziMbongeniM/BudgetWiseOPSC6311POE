package com.example.prog7313_part2.data.local.repository

import com.example.prog7313_part2.data.local.dao.UserDao
import com.example.prog7313_part2.data.local.entities.User
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(firstName: String, lastName: String, email: String, password: String, age: Int): Boolean {
        return try {
            val userCheck = userDao.getUserByEmail(email)
            if (userCheck != null) {
                return false
            }
            val hashedPassword = hashPassword(password)
            val user = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                password = hashedPassword,
                age = age
            )
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loginUser(email: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        return userDao.getUser(email, hashedPassword)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}