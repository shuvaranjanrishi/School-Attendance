package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.*
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM tbl_student ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM tbl_student WHERE id = :id")
    fun getStudentById(id: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM tbl_student WHERE rollNo = :roll AND classCode = :classCode LIMIT 1")
    suspend fun getStudentByRollAndClass(roll: String, classCode: String): StudentEntity?

    @Query("SELECT * FROM tbl_student WHERE name LIKE :query OR rollNo LIKE :query ORDER BY rollNo ASC")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Query(
        """
        SELECT * FROM tbl_student 
        WHERE (name LIKE :query OR rollNo LIKE :query) 
        AND (:classFilter IS NULL OR classCode = :classFilter)
        AND (:gender IS NULL OR genderCode = :gender)
        ORDER BY rollNo ASC"""
    )
    fun searchAndFilterStudents(query: String, classFilter: String?, gender: String?): Flow<List<StudentEntity>>

    @Query("DELETE FROM tbl_student")
    suspend fun deleteAllStudents()
    @Query("SELECT * FROM tbl_student WHERE classCode = :classCode ORDER BY CAST(rollNo AS INTEGER) ASC")
    suspend fun getStudentsByClass(classCode: String): List<StudentEntity>

    // --- ড্যাশবোর্ডের জন্য নতুন কুয়েরি ---
    @Query("SELECT COUNT(*) FROM tbl_student")
    fun getTotalStudentsCount(): Flow<Int>
}
