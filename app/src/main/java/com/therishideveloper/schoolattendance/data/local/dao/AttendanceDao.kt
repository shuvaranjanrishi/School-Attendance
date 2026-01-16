package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.*
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    @Query("UPDATE tbl_attendance SET studentName = :newName WHERE studentId = :id")
    suspend fun updateStudentNameInAttendance(id: Int, newName: String)

    @Query("SELECT * FROM tbl_attendance WHERE classCode = :classCode AND date = :date ORDER BY CAST(rollNo AS INTEGER) ASC")
    fun getAttendanceByClassAndDate(classCode: String, date: String): Flow<List<AttendanceEntity>>

    @Query(
        """
        SELECT 
            :classCode as classCode,
            (SELECT COUNT(*) FROM tbl_student WHERE classCode = :classCode) as totalStudents,
            COUNT(CASE WHEN status = 'Present' THEN 1 END) as totalPresent,
            COUNT(CASE WHEN status = 'Absent' THEN 1 END) as totalAbsent,
            (COUNT(CASE WHEN status = 'Present' OR status = 'Absent' THEN 1 END) > 0) as isTaken
        FROM tbl_attendance 
        WHERE classCode = :classCode AND date = :date
        """
    )
    fun getClassSummary(classCode: String, date: String): Flow<ClassSummary>

    @Query("SELECT COUNT(*) FROM tbl_student")
    fun getTotalStudentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date = :date AND status = 'Present'")
    fun getTotalPresentByDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date = :date AND status = 'Absent'")
    fun getTotalAbsentByDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date LIKE '%' || :monthPattern AND status = 'Present'")
    fun getMonthPresent(monthPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date LIKE '%' || :monthPattern AND status = 'Absent'")
    fun getMonthAbsent(monthPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date LIKE '%' || :yearPattern AND status = 'Present'")
    fun getYearPresent(yearPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tbl_attendance WHERE date LIKE '%' || :yearPattern AND status = 'Absent'")
    fun getYearAbsent(yearPattern: String): Flow<Int>

    @Query("SELECT * FROM tbl_attendance WHERE date LIKE '%' || '-' || :month || '-' || :year")
    fun getMonthlyAttendanceData(month: String, year: String): Flow<List<AttendanceEntity>>
}