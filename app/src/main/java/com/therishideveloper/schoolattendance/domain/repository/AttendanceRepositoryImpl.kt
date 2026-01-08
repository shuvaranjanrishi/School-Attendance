package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.AttendanceDao
import com.therishideveloper.schoolattendance.data.local.dao.StudentDao // এটি ইম্পোর্ট করুন
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val studentDao: StudentDao
) : AttendanceRepository {

    override fun getClassSummary(className: String, date: String): Flow<ClassSummary> {
        return attendanceDao.getClassSummary(className, date)
    }

    override fun getAttendanceRecords(className: String, date: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByClassAndDate(className, date).map { records ->
            records.ifEmpty {
                val students = studentDao.getStudentsByClass(className)
                students.map { student ->
                    AttendanceEntity(
                        studentId = student.id,
                        studentName = student.name,
                        rollNo = student.rollNo,
                        className = student.className,
                        date = date,
                        status = "Present"
                    )
                }
            }
        }
    }

    override suspend fun saveAttendance(records: List<AttendanceEntity>) {
        attendanceDao.insertAttendanceList(records)
    }

    override fun getDashboardSummary(date: String): Flow<DashboardData> {
        return combine(
            attendanceDao.getTotalStudentsCount(),
            attendanceDao.getTotalPresentByDate(date),
            attendanceDao.getTotalAbsentByDate(date)
        ) { total, present, absent ->
            DashboardData(
                total = total,
                present = present,
                absent = absent
            )
        }
    }
    override fun getMonthSummary(month: String): Flow<DashboardData> {
        return combine(
            attendanceDao.getTotalStudentsCount(),
            attendanceDao.getMonthPresent(month + "%"), // মাসের সব দিনের ডেটা
            attendanceDao.getMonthAbsent(month + "%")
        ) { total, present, absent ->
            DashboardData(total, present, absent)
        }
    }

    override fun getYearSummary(year: String): Flow<DashboardData> {
        return combine(
            attendanceDao.getTotalStudentsCount(),
            attendanceDao.getYearPresent(year + "%"), // বছরের সব দিনের ডেটা
            attendanceDao.getYearAbsent(year + "%")
        ) { total, present, absent ->
            DashboardData(total, present, absent)
        }
    }
}