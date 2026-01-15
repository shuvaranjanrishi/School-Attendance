package com.therishideveloper.schoolattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rollNo: String,
    val dateOfBirth: String,
    val age: String,
    val nidOrBirthReg: String,
    val idType: String,
    val classCode: String,
    val fatherName: String = "",
    val motherName: String = "",
    val phone: String = "",
    val religionCode: String = "",
    val genderCode: String = "",
    val bloodGroup: String = "",
    val address: String = "",
    val country: String = "",
    val admissionDate: String = "",
    val image: ByteArray? = null
)