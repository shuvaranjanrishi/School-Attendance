package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.AttendanceDao
import com.therishideveloper.schoolattendance.data.local.dao.UserDao
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import com.therishideveloper.schoolattendance.utils.DateTimeUtils
import com.therishideveloper.schoolattendance.utils.DateTimeUtils.getTodayDate
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao,
    private val attendanceDao: AttendanceDao
) : UserRepository {

    override suspend fun getUser() = dao.getUser()
    override suspend fun insertOrUpdate(user: UserEntity) = dao.insertOrUpdate(user)
    override suspend fun loginUser(email: String, password: String) = dao.loginUser(email, password)
    override suspend fun updatePassword(newPassword: String) {

        val currentUser = dao.getUser()

        currentUser?.let {
            val updatedUser = it.copy(password = newPassword)
            dao.insertOrUpdate(updatedUser)
        }
    }
    override suspend fun isEmailExists(email: String): Boolean {
        return dao.isEmailExists(email)
    }
    override suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }
    override suspend fun getTodayPresentCount(): Int {
        return attendanceDao.getTodayPresentCount(getTodayDate())
    }

    override suspend fun getTotalStudentCount(): Int {
        return attendanceDao.getTotalStudentCount()
    }
}
