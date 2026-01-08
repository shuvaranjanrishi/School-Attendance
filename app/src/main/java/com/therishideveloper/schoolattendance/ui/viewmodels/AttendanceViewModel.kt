package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.utils.ClassTypes
import com.therishideveloper.schoolattendance.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository
) : ViewModel() {

    private val _selectedDate =
        MutableStateFlow(SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()))
    val selectedDate = _selectedDate.asStateFlow()

    fun updateSelectedDate(newDate: String) {
        _selectedDate.value = newDate
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val classSummaries: StateFlow<List<ClassSummary>> = selectedDate.flatMapLatest { date ->
        flow {
            val summaries = ClassTypes.getAll().map { classType ->
                attendanceRepo.getClassSummary(classType.code, date)
            }
            combine(summaries) { it.toList() }.collect { emit(it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _attendanceState = MutableStateFlow<Result<List<AttendanceEntity>>>(Result.Loading)
    val attendanceState = _attendanceState.asStateFlow()

    fun loadStudentsForAttendance(className: String) {
        _attendanceState.value = Result.Loading
        viewModelScope.launch {
            attendanceRepo.getAttendanceRecords(className, _selectedDate.value)
                .catch { e ->
                    _attendanceState.value = Result.Error(e.message ?: "অজানা ত্রুটি")
                }
                .collect { records ->
                    _attendanceState.value = Result.Success(records)
                }

        }
    }

    fun saveAttendance(records: List<AttendanceEntity>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            attendanceRepo.saveAttendance(records)
            onSuccess()
        }
    }

    fun toggleStatus(studentId: Int, currentList: List<AttendanceEntity>) {
        val updatedList = currentList.map {
            if (it.studentId == studentId) {
                it.copy(status = if (it.status == "Present") "Absent" else "Present")
            } else it
        }
        _attendanceState.value = Result.Success(updatedList)
    }

    fun markAllStatus(status: String, currentList: List<AttendanceEntity>) {
        val updatedList = currentList.map { it.copy(status = status) }
        _attendanceState.value = Result.Success(updatedList)
    }
}