package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.export.ExcelExporter
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val pdfGenerator: PdfGenerator,
    private val excelExporter: ExcelExporter
) : ViewModel() {

    val selectedMonth = MutableStateFlow(SimpleDateFormat("MM", Locale.US).format(Date()))
    val selectedYear = MutableStateFlow(SimpleDateFormat("yyyy", Locale.US).format(Date()))

    // সারাংশের জন্য (আগের মতো)
    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyReport = combine(selectedMonth, selectedYear) { month, year ->
        month to year
    }.flatMapLatest { (month, year) ->
        attendanceRepo.getMonthlyReport(month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // বিস্তারিত রিপোর্টের জন্য (নতুন)
    private val _detailedRecords = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val detailedRecords = _detailedRecords.asStateFlow()

    fun loadDetailedReport(className: String) {
        viewModelScope.launch {
            attendanceRepo.getDetailedReport(className, selectedMonth.value, selectedYear.value)
                .collect { records ->
                    _detailedRecords.value = records
                }
        }
    }

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    fun generateMonthlyReportPdf(className: String, displayDate: String) {
        val records = _detailedRecords.value
        if (records.isEmpty()) return

        viewModelScope.launch {
            _isDownloading.value = true
            withContext(Dispatchers.IO) {
                pdfGenerator.downloadMonthlyReportPdf(className, displayDate, records)
            }
            _isDownloading.value = false
        }
    }

    fun generateMonthlyReportExcel(className: String, displayDate: String) {
        val records = _detailedRecords.value
        if (records.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _isDownloading.value = true
            excelExporter.exportAttendanceReportToExcel(className, displayDate, records)
            _isDownloading.value = false
        }
    }

    fun updateDate(month: String, year: String) {
        selectedMonth.value = month
        selectedYear.value = year
    }
}