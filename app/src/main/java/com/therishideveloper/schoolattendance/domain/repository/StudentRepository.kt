package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun addStudent(student: StudentEntity)
    fun getAllStudents(): Flow<List<StudentEntity>>
    suspend fun deleteStudent(student: StudentEntity)

    suspend fun updateStudent(student: StudentEntity)

    fun getStudentById(id: Int): Flow<StudentEntity?>

    suspend fun getStudentByRollAndClass(roll: String, className: String): StudentEntity?

    fun searchStudents(query: String): Flow<List<StudentEntity>>

    fun searchAndFilterStudents(
        query: String,
        classFilter: String?,
        genderFilter: String?
    ): Flow<List<StudentEntity>>
    suspend fun clearAllData()
    suspend fun getStudentsByClass(className: String): List<StudentEntity>
    fun getTotalStudentsCount(): Flow<Int>
}