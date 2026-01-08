package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepository
import com.therishideveloper.schoolattendance.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchoolViewModel @Inject constructor(
    private val repository: SchoolRepository
) : ViewModel() {

    // স্ক্রিনের ডাটা স্টেট (StudentViewModel এর মতো Result প্যাটার্ন)
    private val _schoolState = MutableStateFlow<Result<SchoolEntity?>>(Result.Loading)
    val schoolState = _schoolState.asStateFlow()

    init {
        loadSchoolData()
    }

    // ডাটাবেস থেকে ডাটা লোড করার ফাংশন
    fun loadSchoolData() {
        viewModelScope.launch {
            _schoolState.value = Result.Loading
            try {
                val school = repository.getSchool()
                _schoolState.value = Result.Success(school)
            } catch (e: Exception) {
                _schoolState.value = Result.Error(e.message ?: "Unknown Error")
            }
        }
    }

    // আপনার রিপোজিটরির insertOrUpdate কল করা
    fun insertOrUpdate(school: SchoolEntity) {
        viewModelScope.launch {
            repository.insertOrUpdate(school)
            // ডাটা সেভ হওয়ার পর স্টেট আপডেট করে দেওয়া যাতে স্ক্রিনে পরিবর্তন দেখা যায়
            loadSchoolData()
        }
    }
}