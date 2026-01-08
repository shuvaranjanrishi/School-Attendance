package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("SELECT * FROM user_profile WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): UserEntity?

    @Query("UPDATE user_profile SET password = :newPass WHERE id = :userId")
    suspend fun updatePassword(userId: Int, newPass: String)

    @Query("SELECT password FROM user_profile WHERE id = :userId LIMIT 1")
    suspend fun getPassword(userId: Int): String?

    @Query("SELECT EXISTS(SELECT 1 FROM user_profile WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    @Query("SELECT * FROM user_profile WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
}