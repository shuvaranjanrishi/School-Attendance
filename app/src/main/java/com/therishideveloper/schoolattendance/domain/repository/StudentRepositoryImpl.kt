package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.StudentDao
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudentRepositoryImpl @Inject constructor(
    private val dao: StudentDao
) : StudentRepository {
    override suspend fun addStudent(student: StudentEntity) {
        dao.insertStudent(student)
    }

    override fun getAllStudents(): Flow<List<StudentEntity>> {
        return dao.getAllStudents()
    }

    override suspend fun deleteStudent(student: StudentEntity) {
        dao.deleteStudent(student)
    }

    override suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
    }

    override fun getStudentById(id: Int): Flow<StudentEntity?> {
        return dao.getStudentById(id)
    }

    override suspend fun getStudentByRollAndClass(roll: String, className: String): StudentEntity? {
        return dao.getStudentByRollAndClass(roll, className)
    }

    override fun searchStudents(query: String): Flow<List<StudentEntity>> {
        return dao.searchStudents(query)
    }

    override fun searchAndFilterStudents(
        query: String,
        classFilter: String?,
        genderFilter: String?
    ): Flow<List<StudentEntity>> {
        return dao.searchAndFilterStudents(query, classFilter, genderFilter)
    }
    override suspend fun clearAllData() {
        dao.deleteAllStudents()
    }
    override suspend fun getStudentsByClass(className: String): List<StudentEntity> {
        return dao.getStudentsByClass(className)
    }
    override fun getTotalStudentsCount(): Flow<Int> {
        return dao.getTotalStudentsCount()
    }
}