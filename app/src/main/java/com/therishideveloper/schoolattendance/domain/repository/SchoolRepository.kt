package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity

interface SchoolRepository {
    suspend fun getSchool(): SchoolEntity?
    suspend fun insertOrUpdate(school: SchoolEntity)
}