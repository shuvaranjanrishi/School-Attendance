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

    override fun getAttendanceRecords(
        className: String,
        date: String
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByClassAndDate(className, date).map { records ->
            records.ifEmpty {
                val students = studentDao.getStudentsByClass(className)
                students.map { student ->
                    AttendanceEntity(
                        studentId = student.id,
                        studentName = student.name,
                        rollNo = student.rollNo,
                        genderCode = student.genderCode,
                        classCode = student.classCode,
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
    override suspend fun updateStudentNameInAttendance(id:Int,name: String) {
        attendanceDao.updateStudentNameInAttendance( id,name)
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
        // এখানে DAO এর সেই একটি কমন ফাংশন কল করছি
        return attendanceDao.getMonthlyAttendanceData(month, year).map { entities ->
            entities.groupBy { it.classCode }.map { (className, records) ->
                // ... আপনার আগের ক্যালকুলেশন লজিক (total, present, absent ইত্যাদি) ...
                // এটি গ্রাফ বা ক্লাস সামারি স্ক্রিনে দেখাবে
                calculateMonthlyReportModel(
                    className,
                    records
                ) // একটি হেল্পার ফাংশন হিসেবে রাখতে পারেন
            }
        }
    }

    // এটি নির্দিষ্ট একটি ক্লাসের সব রেকর্ড ফিল্টার করে দেয়
    override fun getDetailedReport(
        className: String,
        month: String,
        year: String
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getMonthlyAttendanceData(month, year).map { allRecords ->
            // সব ডাটা থেকে শুধু ওই ক্লাসের ডাটা ফিল্টার করে দিচ্ছি
            allRecords.filter { it.classCode == className }
        }
    }

    // এটি সব ক্লাসের ডাটা সরাসরি পাঠিয়ে দেয় (আপনার নতুন স্টুডেন্ট-ওয়াইজ রিপোর্টের জন্য)
    override fun getAllDetailedReportByMonth(
        month: String,
        year: String
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getMonthlyAttendanceData(month, year)
    }

    private fun calculateMonthlyReportModel(className: String, records: List<AttendanceEntity>): MonthlyReportModel {
        // ১. গ্র্যান্ড টোটাল
        val total = records.size
        val present = records.count { it.status == "Present" }
        val absent = total - present

        // ২. ছেলেদের হিসাব
        val bTotal = records.count { it.genderCode == GenderTypes.MALE.code }
        val bPresent = records.count { it.genderCode == GenderTypes.MALE.code && it.status == "Present" }
        val bAbsent = bTotal - bPresent

        // ৩. মেয়েদের হিসাব
        val gTotal = records.count { it.genderCode == GenderTypes.FEMALE.code }
        val gPresent = records.count { it.genderCode == GenderTypes.FEMALE.code && it.status == "Present" }
        val gAbsent = gTotal - gPresent

        // ৪. উপস্থিতির হার
        val rate = if (total > 0) (present.toFloat() / total * 100f) else 0f

        return MonthlyReportModel(
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