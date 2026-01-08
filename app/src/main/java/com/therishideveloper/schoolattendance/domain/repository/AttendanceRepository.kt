package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getClassSummary(className: String, date: String): Flow<ClassSummary>

    fun getAttendanceRecords(className: String, date: String): Flow<List<AttendanceEntity>>

    suspend fun saveAttendance(records: List<AttendanceEntity>)

    fun getDashboardSummary(date: String): Flow<DashboardData>
}
