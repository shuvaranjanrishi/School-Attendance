package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

interface UserRepository {
    suspend fun getUser(): UserEntity?
    suspend fun insertOrUpdate(user: UserEntity)
    suspend fun loginUser(email: String, password: String): UserEntity?
    suspend fun updatePassword(newPassword: String)
    suspend fun isEmailExists(email: String): Boolean
    suspend fun getUserByEmail(email: String): UserEntity?
}