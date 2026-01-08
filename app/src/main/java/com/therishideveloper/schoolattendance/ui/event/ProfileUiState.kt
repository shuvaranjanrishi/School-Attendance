package com.therishideveloper.schoolattendance.ui.event

import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

/**
 * State class to hold the UI data for the Profile Screen.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserEntity? = null,
    // Dummy stats data for the Statistics Summary section
    val totalClasses: String = "145",
    val attendanceDone: String = "132",
    val avgPresence: String = "88%",
    val errorMessage: String? = null
)