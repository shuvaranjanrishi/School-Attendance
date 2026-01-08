package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.UserDao
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dao: UserDao
) : UserRepository {

    override suspend fun getUser() = dao.getUser()
    override suspend fun insertOrUpdate(user: UserEntity) = dao.insertOrUpdate(user)
    override suspend fun loginUser(email: String, password: String) = dao.loginUser(email, password)
    override suspend fun updatePassword(newPassword: String) {
        // Fetch the current user profile from database
        val currentUser = dao.getUser()

        currentUser?.let {
            // Create a copy of the user with the new password
            val updatedUser = it.copy(password = newPassword)

            // Save the updated entity back to Room
            dao.insertOrUpdate(updatedUser)
        }
    }
    override suspend fun isEmailExists(email: String): Boolean {
        return dao.isEmailExists(email)
    }
    override suspend fun getUserByEmail(email: String): UserEntity? {
        return dao.getUserByEmail(email)
    }
}
