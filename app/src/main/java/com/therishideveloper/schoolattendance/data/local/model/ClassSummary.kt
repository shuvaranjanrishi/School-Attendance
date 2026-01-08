package com.therishideveloper.schoolattendance.data.local.model

data class ClassSummary(
    val className: String,
    val totalStudents: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val isTaken: Boolean = (totalPresent + totalAbsent) > 0
)