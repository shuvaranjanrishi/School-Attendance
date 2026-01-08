package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.model.DashboardData // আপনার মডেলের নাম অনুযায়ী
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: AttendanceRepository
) : ViewModel() {

    private val today = DateUtils.getTodayDate()
    private val currentMonth = DateUtils.getCurrentMonth()
    private val currentYear = DateUtils.getCurrentYear()

    // HomeViewModel-এর ভেতর
    // HomeViewModel এর ভেতর এই StateFlow টি যোগ করুন
    private val _selectedDate = MutableStateFlow(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    val selectedDate = _selectedDate.asStateFlow()

    // ১. আজকের সামারি
    val dashboardSummary = repository.getDashboardSummary(today)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardData(0, 0, 0)
        )

    // ২. এই মাসের সামারি
    val monthSummary = repository.getMonthSummary(currentMonth)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardData(0, 0, 0)
        )

    // ৩. এই বছরের সামারি
    val yearSummary = repository.getYearSummary(currentYear)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardData(0, 0, 0)
        )
}