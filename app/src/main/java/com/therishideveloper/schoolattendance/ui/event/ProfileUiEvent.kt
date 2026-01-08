package com.therishideveloper.schoolattendance.ui.event

import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

/**
 * Sealed class to represent all possible user interactions on the Profile Screen.
 */
sealed class ProfileUiEvent {
    // Event to save or update the profile
    data class SaveProfile(val user: UserEntity) : ProfileUiEvent()

    // Event to trigger profile data loading
    object LoadProfile : ProfileUiEvent()

    // Event to handle image picking (we will implement this in UI)
    data class OnImageSelected(val bytes: ByteArray?) : ProfileUiEvent()
}