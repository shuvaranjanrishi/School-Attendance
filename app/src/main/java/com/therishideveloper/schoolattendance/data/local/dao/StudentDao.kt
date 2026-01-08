package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.*
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentById(id: Int): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE rollNo = :roll AND className = :className LIMIT 1")
    suspend fun getStudentByRollAndClass(roll: String, className: String): StudentEntity?

    @Query("SELECT * FROM students WHERE name LIKE :query OR rollNo LIKE :query ORDER BY rollNo ASC")
    fun searchStudents(query: String): Flow<List<StudentEntity>>

    @Query("""
        SELECT * FROM students 
        WHERE (name LIKE :query OR rollNo LIKE :query) 
        AND (:classFilter IS NULL OR className = :classFilter)
        AND (:gender IS NULL OR gender = :gender)
        ORDER BY rollNo ASC""")
    fun searchAndFilterStudents(query: String, classFilter: String?, gender: String?): Flow<List<StudentEntity>>

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
    @Query("SELECT * FROM students WHERE className = :className ORDER BY CAST(rollNo AS INTEGER) ASC")
    suspend fun getStudentsByClass(className: String): List<StudentEntity>

    // --- ড্যাশবোর্ডের জন্য নতুন কুয়েরি ---
    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentsCount(): Flow<Int>
}
