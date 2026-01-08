package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AttendanceRepository
) : ViewModel() {
    private val today = DateUtils.getTodayDate()

    val dashboardSummary = repository.getDashboardSummary(today)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardData(0, 0, 0)
        )
}