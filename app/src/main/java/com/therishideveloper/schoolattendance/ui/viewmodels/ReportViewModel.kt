package com.therishideveloper.schoolattendance.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.export.ExcelExporter
import com.therishideveloper.schoolattendance.data.local.model.StudentMonthlySummary
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.utils.PdfGenerator
import com.therishideveloper.schoolattendance.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val attendanceRepo: AttendanceRepository,
    private val pdfGenerator: PdfGenerator,
    private val excelExporter: ExcelExporter
) : ViewModel() {

    // ১. স্টেট কন্ট্রোল
    val selectedMonth = MutableStateFlow(SimpleDateFormat("MM", Locale.US).format(Date()))
    val selectedYear = MutableStateFlow(SimpleDateFormat("yyyy", Locale.US).format(Date()))

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedClassFilter = MutableStateFlow("All")
    val selectedClassFilter = _selectedClassFilter.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading = _isDownloading.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyReport = combine(selectedMonth, selectedYear) { month, year ->
        month to year
    }.flatMapLatest { (month, year) ->
        attendanceRepo.getMonthlyReport(month, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val detailedRecords: StateFlow<List<AttendanceEntity>> =
        combine(selectedMonth, selectedYear) { month, year ->
            month to year
        }.flatMapLatest { (month, year) ->
            attendanceRepo.getAllDetailedReportByMonth(month, year)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ৩. স্টুডেন্ট সামারি লজিক (অটোমেটিক আপডেট হবে)
    val studentSummaries: StateFlow<List<StudentMonthlySummary>> = detailedRecords.map { records ->
        records.groupBy { it.studentId }.map { (id, studentRecords) ->
            val first = studentRecords.first()
            val total = studentRecords.size
            val present = studentRecords.count { it.status == "Present" }
            val percentage = if (total > 0) (present.toFloat() / total) * 100f else 0f

            StudentMonthlySummary(
                studentId = id,
                name = first.studentName,
                rollNo = first.rollNo,
                className = first.className,
                totalDays = total,
                presentCount = present,
                absentCount = total - present,
                attendancePercentage = percentage
            )
        }.sortedWith(compareBy({ it.className }, { it.rollNo.toIntOrNull() ?: 0 }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ৪. ফিল্টার করা স্টুডেন্ট লিস্ট (UI তে দেখানোর জন্য)
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredStudentSummaries = combine(
        studentSummaries,
        _searchQuery,
        _selectedClassFilter
    ) { summaries, query, cls ->
        summaries.filter { student ->
            val matchesClass = if (cls == "All") true else student.className == cls
            val matchesQuery = student.name.contains(query, ignoreCase = true) ||
                    student.rollNo.contains(query)
            matchesClass && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- অ্যাকশন ফাংশনস ---

    fun onSearch(query: String) {
        _searchQuery.value = query
    }

    fun onClassFilterChanged(className: String) {
        _selectedClassFilter.value = className
    }

    fun updateDate(month: String, year: String) {
        selectedMonth.value = month
        selectedYear.value = year
        // flatMapLatest এর কারণে ডাটা অটোমেটিক লোড হবে
    }

    // ৫. পিডিএফ ও এক্সেল জেনারেশন (ClassName প্যারামিটার সহ)
    fun generateMonthlyReportPdf(className: String, displayDate: String) {
        val records =
            detailedRecords.value.filter { it.className == className || className == "All" || className == "All Classes" }
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
        val records =
            detailedRecords.value.filter { it.className == className || className == "All" || className == "All Classes" }
        if (records.isEmpty()) return

        viewModelScope.launch {
            _isDownloading.value = true
            withContext(Dispatchers.IO) {
                excelExporter.exportAttendanceReportToExcel(className, displayDate, records)
            }
            _isDownloading.value = false
        }
    }

    // ViewModel এর ভেতর এই দুটি ফাংশন আপডেট করুন
    fun downloadStudentCalendarPdf(
        summary: StudentMonthlySummary,
        displayDate: String,
        records: List<AttendanceEntity> // ক্যালেন্ডারের জন্য লিস্টটি পাঠাতে হবে
    ) {
        viewModelScope.launch {
            _isDownloading.value = true
            withContext(Dispatchers.IO) {
                pdfGenerator.createStudentMonthlyDetailsReport(
                    studentName = summary.name,
                    className = summary.className,
                    monthYear = displayDate,
                    rollNo = summary.rollNo,
                    total = summary.totalDays,
                    present = summary.presentCount,
                    absent = summary.absentCount,
                    percent = summary.attendancePercentage,
                    records = records,
                    isSharing = false
                )
            }
            _isDownloading.value = false
        }
    }

    // Share ফাংশনটিও একইভাবে আপডেট করুন
    fun shareStudentCalendarPdf(
        summary: StudentMonthlySummary,
        displayDate: String,
        records: List<AttendanceEntity>
    ) {
        viewModelScope.launch {
            _isDownloading.value = true
            val result = withContext(Dispatchers.IO) {
                pdfGenerator.createStudentMonthlyDetailsReport(
                    summary.name, summary.className, displayDate, summary.rollNo,
                    summary.totalDays, summary.presentCount, summary.absentCount,
                    summary.attendancePercentage, records, isSharing = true
                )
            }
            if (result is Result.Success) {
                withContext(Dispatchers.Main) { pdfGenerator.shareFile(result.data) }
            }
            _isDownloading.value = false
        }
    }

    fun getStudentDetails(
        studentId: Int,
        records: List<AttendanceEntity>
    ): Pair<StudentMonthlySummary?, Triple<Int, Int, Int>> {
        val specificRecords = records.filter { it.studentId == studentId }
        val studentInfo = specificRecords.firstOrNull()

        val total = specificRecords.size
        val present = specificRecords.count { it.status == "Present" }
        val absent = total - present
        val percent = if (total > 0) (present.toFloat() / total) * 100f else 0f

        val summary = studentInfo?.let {
            StudentMonthlySummary(
                studentId = studentId,
                name = it.studentName,
                rollNo = it.rollNo,
                className = it.className,
                totalDays = total,
                presentCount = present,
                absentCount = absent,
                attendancePercentage = percent
            )
        }
        return summary to Triple(total, present, absent)
    }
}


