package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.SettingsManager
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import com.therishideveloper.schoolattendance.domain.repository.UserRepository
import com.therishideveloper.schoolattendance.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _userState = MutableStateFlow<Result<UserEntity?>>(Result.Loading)
    val userState = _userState.asStateFlow()

    private val _authResult = MutableSharedFlow<Result<Unit>>()
    val authResult = _authResult.asSharedFlow()

    init {
        loadProfileData(showLoading = true)
    }

    // Load user profile from database
    fun loadProfileData(showLoading: Boolean = false) {
        viewModelScope.launch {
            if (showLoading) _userState.value = Result.Loading
            try {
                val user = repository.getUser()
                _userState.value = Result.Success(user)
            } catch (e: Exception) {
                _userState.value = Result.Error(e.message ?: "Unknown Error")
            }
        }
    }

    // New: Fetch user by email for password recovery
    suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            repository.getUserByEmail(email) // Ensure this exists in Repository/DAO
        } catch (e: Exception) {
            null
        }
    }

    // Insert or update user entity
    fun insertOrUpdate(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdate(user)
                loadProfileData(showLoading = false)
                _authResult.emit(Result.Success(Unit)) // Notify success for password updates
            } catch (e: Exception) {
                _userState.value = Result.Error(e.message ?: "Update Failed")
            }
        }
    }

    // Professional Auth with Security Question & Duplicate Check
    fun performAuthWithSecurity(
        email: String,
        password: String,
        isSignUp: Boolean,
        securityQuestion: String? = null,
        securityAnswer: String? = null
    ) {
        viewModelScope.launch {
            try {
                if (isSignUp) {
                    // Check if email is already taken
                    val emailExists = repository.isEmailExists(email)
                    if (emailExists) {
                        _authResult.emit(Result.Error("EMAIL_EXISTS"))
                        return@launch
                    }

                    // Creating new user with Security Question/Answer
                    val newUser = UserEntity(
                        id = 1,
                        name = "",
                        designation = "",
                        qualification = "",
                        teacherId = "",
                        joiningDate = "",
                        jobDuration = "",
                        assignedClasses = "",
                        subjectExpert = "",
                        phone = "",
                        email = email,
                        password = password,
                        securityQuestion = securityQuestion, // Update Entity to include this
                        securityAnswer = securityAnswer,     // Update Entity to include this
                        image = null
                    )
                    repository.insertOrUpdate(newUser)
                    settingsManager.saveLoginStatus(true)
                    _authResult.emit(Result.Success(Unit))
                } else {
                    // Login Process
                    val user = repository.loginUser(email, password)
                    if (user != null) {
                        settingsManager.saveLoginStatus(true)
                        _authResult.emit(Result.Success(Unit))
                    } else {
                        _authResult.emit(Result.Error("INVALID_CREDENTIALS"))
                    }
                }
            } catch (e: Exception) {
                _authResult.emit(Result.Error(e.message ?: "Authentication Failed"))
            }
        }
    }

    // Basic logout logic
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsManager.saveLoginStatus(false)
            onComplete()
        }
    }
}
