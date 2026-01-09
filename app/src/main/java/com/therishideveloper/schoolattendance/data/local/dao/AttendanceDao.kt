package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.*
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(list: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance_records WHERE className = :className AND date = :date ORDER BY CAST(rollNo AS INTEGER) ASC")
    fun getAttendanceByClassAndDate(className: String, date: String): Flow<List<AttendanceEntity>>

    @Query("""
        SELECT 
            :className as className,
            (SELECT COUNT(*) FROM students WHERE className = :className) as totalStudents,
            COUNT(CASE WHEN status = 'Present' THEN 1 END) as totalPresent,
            COUNT(CASE WHEN status = 'Absent' THEN 1 END) as totalAbsent,
            (COUNT(CASE WHEN status = 'Present' OR status = 'Absent' THEN 1 END) > 0) as isTaken
        FROM attendance_records 
        WHERE className = :className AND date = :date
        """)
    fun getClassSummary(className: String, date: String): Flow<ClassSummary>

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'Present'")
    fun getTotalPresentByDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date = :date AND status = 'Absent'")
    fun getTotalAbsentByDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date LIKE '%' || :monthPattern AND status = 'Present'")
    fun getMonthPresent(monthPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date LIKE '%' || :monthPattern AND status = 'Absent'")
    fun getMonthAbsent(monthPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date LIKE '%' || :yearPattern AND status = 'Present'")
    fun getYearPresent(yearPattern: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE date LIKE '%' || :yearPattern AND status = 'Absent'")
    fun getYearAbsent(yearPattern: String): Flow<Int>

    @Query("SELECT * FROM attendance_records WHERE date LIKE '%-' || :month || '-' || :year")
    fun getMonthlyReportData(month: String, year: String): Flow<List<AttendanceEntity>>

    // নির্দিষ্ট মাস, বছর এবং ক্লাসের সব হাজিরা একসাথে পাওয়ার জন্য
    @Query("""
            SELECT * FROM attendance_records 
            WHERE className = :className 
            AND date LIKE '%-' || :month || '-' || :year
            ORDER BY studentId ASC, date ASC
        """)
    fun getDetailedMonthlyReport(className: String, month: String, year: String): Flow<List<AttendanceEntity>>
}