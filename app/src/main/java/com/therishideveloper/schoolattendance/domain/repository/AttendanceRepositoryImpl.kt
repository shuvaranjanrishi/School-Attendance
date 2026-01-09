package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.AttendanceDao
import com.therishideveloper.schoolattendance.data.local.dao.StudentDao // এটি ইম্পোর্ট করুন
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.model.ClassSummary
import com.therishideveloper.schoolattendance.data.local.model.DashboardData
import com.therishideveloper.schoolattendance.data.local.model.MonthlyReportModel
import com.therishideveloper.schoolattendance.utils.GenderTypes
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
                        gender = student.gender,
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

    override fun getMonthlyReport(month: String, year: String): Flow<List<MonthlyReportModel>> {
        return attendanceDao.getMonthlyReportData(month, year).map { entities ->
            entities.groupBy { it.className }.map { (className, records) ->

                // ১. গ্র্যান্ড টোটাল ক্যালকুলেশন
                val total = records.size
                val present = records.count { it.status == "Present" }
                val absent = total - present

                // ২. ছেলেদের জন্য ক্যালকুলেশন (GenderTypes.MALE.code অর্থাৎ "MALE" এর সাথে ম্যাচ করানো)
                val bTotal = records.count { it.gender == GenderTypes.MALE.code }
                val bPresent = records.count { it.gender == GenderTypes.MALE.code && it.status == "Present" }
                val bAbsent = bTotal - bPresent

                // ৩. মেয়েদের জন্য ক্যালকুলেশন (GenderTypes.FEMALE.code অর্থাৎ "FEMALE" এর সাথে ম্যাচ করানো)
                val gTotal = records.count { it.gender == GenderTypes.FEMALE.code }
                val gPresent = records.count { it.gender == GenderTypes.FEMALE.code && it.status == "Present" }
                val gAbsent = gTotal - gPresent

                // ৪. উপস্থিতির হার
                val rate = if (total > 0) (present.toFloat() / total * 100f) else 0f

                MonthlyReportModel(
                    className = className,
                    totalAttendance = total,
                    presentCount = present,
                    absentCount = absent,
                    boysTotal = bTotal,
                    boysPresent = bPresent,
                    boysAbsent = bAbsent,
                    girlsTotal = gTotal,
                    girlsPresent = gPresent,
                    girlsAbsent = gAbsent,
                    percentage = rate
                )
            }
        }
    }

    override fun getDetailedReport(
        className: String,
        month: String,
        year: String
    ): Flow<List<AttendanceEntity>> {
        // সরাসরি DAO কল করে নির্দিষ্ট ক্লাস এবং মাসের সব রেকর্ড নিয়ে আসা হচ্ছে
        return attendanceDao.getDetailedMonthlyReport(className, month, year)
    }

}