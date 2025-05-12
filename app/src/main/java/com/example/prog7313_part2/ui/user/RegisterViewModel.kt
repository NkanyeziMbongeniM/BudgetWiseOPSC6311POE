package com.example.prog7313_part2.ui.user

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.prog7313_part2.data.local.AppDatabase
import com.example.prog7313_part2.data.local.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val _registrationResult = MutableLiveData<Boolean>()
    val registrationResult: LiveData<Boolean> get() = _registrationResult

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun register(firstName: String, lastName: String, email: String, password: String, age: Int) {
        viewModelScope.launch {
            val success = userRepository.registerUser(firstName, lastName, email, password, age)
            _registrationResult.postValue(success)
        }
    }
}