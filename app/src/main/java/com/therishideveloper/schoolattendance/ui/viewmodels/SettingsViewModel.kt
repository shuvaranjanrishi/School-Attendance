package com.therishideveloper.schoolattendance.ui.viewmodels

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.AppDatabase
import com.therishideveloper.schoolattendance.data.local.export.ExcelExporter
import com.therishideveloper.schoolattendance.data.local.AppLanguage
import com.therishideveloper.schoolattendance.data.local.ThemeMode
import com.therishideveloper.schoolattendance.data.local.SettingsManager
import com.therishideveloper.schoolattendance.data.local.backup.DatabaseBackupManager
import com.therishideveloper.schoolattendance.domain.repository.StudentRepository
import com.therishideveloper.schoolattendance.domain.repository.UserRepository
import com.therishideveloper.schoolattendance.ui.event.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.therishideveloper.schoolattendance.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val excelExporter: ExcelExporter,
    private val repository: StudentRepository,
    private val backupManager: DatabaseBackupManager,
    private val database: AppDatabase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _toastEvent = MutableSharedFlow<UiEvent>()
    val toastEvent = _toastEvent.asSharedFlow()

    val isLoggedInFlow = settingsManager.isLoggedInFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null // শুরুতে নাল থাকবে যাতে লোডিং স্ক্রিন দেখানো যায়
        )

    val currentLanguage: StateFlow<AppLanguage> = settingsManager.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.ENGLISH
        )

    val themeMode: StateFlow<ThemeMode> = settingsManager.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsManager.saveLanguage(language)
        }
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            settingsManager.saveTheme(theme)
        }
    }

    fun getString(context: Context, resId: Int): String {
        val locale = Locale(currentLanguage.value.code)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources.getString(resId)
    }

    fun performBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = backupManager.backupDatabase()) {
                is Result.Success -> {
                    _toastEvent.emit(UiEvent.ShowToastRes(R.string.backup_success_msg))
                }

                is Result.Error -> {
                    _toastEvent.emit(UiEvent.ShowToastStr("Error: ${result.message}"))
                }

                else -> Unit
            }
        }
    }

    fun performRestore() {
        viewModelScope.launch(Dispatchers.IO) {
            database.close()
            when (val result = backupManager.restoreDatabase()) {
                is Result.Success -> {
                    _toastEvent.emit(UiEvent.ShowRestartDialog)
                }

                is Result.Error -> {
                    _toastEvent.emit(UiEvent.ShowToastStr("Error: ${result.message}"))
                }

                else -> Unit
            }
        }

    }

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    fun exportToExcel() {
        viewModelScope.launch(Dispatchers.IO) {
            _isExporting.value = true

            val students = repository.getAllStudents().first()// সব ডাটা রিড করা
            val result = excelExporter.exportStudentsToExcel(students)

            _isExporting.value = false

            when (result) {
                is Result.Success -> {
                    _toastEvent.emit(UiEvent.ShowToastRes(R.string.excel_save_success))
                }

                is Result.Error -> {
                    _toastEvent.emit(UiEvent.ShowToastStr("Error: ${result.message}"))
                }

                else -> Unit
            }
        }
    }

    fun clearAllStudents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.clearAllTables()
                _toastEvent.emit(UiEvent.ShowToastRes(R.string.all_data_cleared)) // এই স্ট্রিংটি নিচে দিচ্ছি
            } catch (e: Exception) {
                _toastEvent.emit(UiEvent.ShowToastStr("Error: ${e.message}"))
            }
        }
    }

    // Function to handle password update logic
    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                // Fetch current user from database
                // English Comment: Get the currently logged-in user profile
                val user = userRepository.getUser()

                if (user != null) {
                    // Verify if the current password matches
                    // English Comment: Compare the user-inputted old password with the stored password
                    if (user.password == oldPassword) {

                        // Update the password in the repository
                        // English Comment: Success case - verification passed, now updating password
                        userRepository.updatePassword(newPassword)

                        // Show success toast
                        _toastEvent.emit(UiEvent.ShowToastRes(R.string.password_update_success))
                    } else {
                        // Show error for incorrect password
                        // English Comment: Error case - the old password does not match the database record
                        _toastEvent.emit(UiEvent.ShowToastRes(R.string.error_incorrect_password))
                    }
                } else {
                    // Show error if user profile is missing
                    // English Comment: Edge case - no user record exists in the UserTable
                    _toastEvent.emit(UiEvent.ShowToastRes(R.string.error_user_not_found))
                }
            } catch (e: Exception) {
                // Handle unexpected exceptions
                // English Comment: Catch any database or thread-related exceptions
                _toastEvent.emit(UiEvent.ShowToastRes(R.string.error_unknown))
            }
        }
    }
}