package com.therishideveloper.schoolattendance.data.local.entity

/**
 * Data class for UI-only statistics summary.
 */
data class UserStats(
    val totalClasses: String = "120",
    val attendanceCompleted: String = "115",
    val averagePresence: String = "92%"
)