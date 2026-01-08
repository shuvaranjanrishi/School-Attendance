package com.therishideveloper.schoolattendance.ui.event

sealed class UiEvent {
    data class ShowToastRes(val resId: Int) : UiEvent()
    data class ShowToastStr(val message: String) : UiEvent()
    data object ShowRestartDialog : UiEvent()
}