package com.therishideveloper.schoolattendance.data.local.model

data class StudentMonthlySummary(
    val studentId: Int,
    val name: String,
    val rollNo: String,
    val classCode: String,
    val totalDays: Int,
    val presentCount: Int,
    val absentCount: Int,
    val attendancePercentage: Float
)