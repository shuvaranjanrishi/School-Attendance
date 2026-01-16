package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import com.therishideveloper.schoolattendance.data.local.model.MonthlyReportModel
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getClassSummary(classCode: String, date: String): Flow<ClassSummary>

    fun getAttendanceRecords(classCode: String, date: String): Flow<List<AttendanceEntity>>

    suspend fun saveAttendance(records: List<AttendanceEntity>)
    suspend fun updateStudentNameInAttendance(id: Int, name: String)

    fun getDashboardSummary(date: String): Flow<DashboardData>
    fun getMonthSummary(month: String): Flow<DashboardData>
    fun getYearSummary(year: String): Flow<DashboardData>

    fun getMonthlyReport(month: String, year: String): Flow<List<MonthlyReportModel>>
    fun getDetailedReport(classCode: String, month: String, year: String): Flow<List<AttendanceEntity>>
    fun getAllDetailedReportByMonth(month: String, year: String): Flow<List<AttendanceEntity>>
}
