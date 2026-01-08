package com.therishideveloper.schoolattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val name: String,
    val designation: String,
    val qualification: String,
    val teacherId: String,
    val joiningDate: String,
    val jobDuration: String,
    val assignedClasses: String,
    val subjectExpert: String,
    val phone: String,
    val email: String,
    val password: String,
    val securityQuestion: String? = null,
    val securityAnswer: String? = null,
    val image: ByteArray? = null
)