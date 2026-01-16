package com.therishideveloper.schoolattendance.data.local.entity

/**
 * Data class for UI-only statistics summary.
 */
data class UserStats(
    val totalPresent: Int = 0,
    val totalAbsent: Int = 0,
    val attendanceRate: String = "0%"
)