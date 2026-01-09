package com.therishideveloper.schoolattendance.data.local.model

data class MonthlyReportModel(
    val className: String,
    val totalAttendance: Int,
    val presentCount: Int,
    val absentCount: Int,
    val boysTotal: Int,
    val boysPresent: Int,
    val boysAbsent: Int,
    val girlsTotal: Int,
    val girlsPresent: Int,
    val girlsAbsent: Int,
    val percentage: Float
)