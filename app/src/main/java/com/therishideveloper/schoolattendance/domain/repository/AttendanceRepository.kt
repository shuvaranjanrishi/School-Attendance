package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import com.therishideveloper.schoolattendance.data.local.model.MonthlyReportModel
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getClassSummary(className: String, date: String): Flow<ClassSummary>

    fun getAttendanceRecords(className: String, date: String): Flow<List<AttendanceEntity>>

    suspend fun saveAttendance(records: List<AttendanceEntity>)

    fun getDashboardSummary(date: String): Flow<DashboardData>
    fun getMonthSummary(month: String): Flow<DashboardData>
    fun getYearSummary(year: String): Flow<DashboardData>

    // AttendanceRepository.kt
    fun getMonthlyReport(month: String, year: String): Flow<List<MonthlyReportModel>>
    fun getDetailedReport(className: String, month: String, year: String): Flow<List<AttendanceEntity>>
}
