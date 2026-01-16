package com.therishideveloper.schoolattendance.data.local.model

data class ClassSummary(
    val classCode: String,
    val totalStudents: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val isTaken: Boolean = (totalPresent + totalAbsent) > 0
)