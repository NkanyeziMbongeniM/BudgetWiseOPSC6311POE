package com.example.prog7313_part2.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.entities.User
import com.example.prog7313_part2.data.local.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> get() = _loginResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = userRepository.loginUser(email, password)
            _loginResult.postValue(user)
        }
    }
}