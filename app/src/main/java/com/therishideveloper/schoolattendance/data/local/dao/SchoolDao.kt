package com.therishideveloper.schoolattendance.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity

@Dao
interface SchoolDao {
    @Query("SELECT * FROM school_profile WHERE id = 1")
    suspend fun getSchoolProfile(): SchoolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: SchoolEntity)
}