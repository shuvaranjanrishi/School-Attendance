package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM tbl_user WHERE id = 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: UserEntity)

    @Query("SELECT * FROM tbl_user WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): UserEntity?

    @Query("UPDATE tbl_user SET password = :newPass WHERE id = :userId")
    suspend fun updatePassword(userId: Int, newPass: String)

    @Query("SELECT password FROM tbl_user WHERE id = :userId LIMIT 1")
    suspend fun getPassword(userId: Int): String?

    @Query("SELECT EXISTS(SELECT 1 FROM tbl_user WHERE email = :email)")
    suspend fun isEmailExists(email: String): Boolean

    @Query("SELECT * FROM tbl_user WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?
}