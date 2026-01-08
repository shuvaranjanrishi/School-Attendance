package com.therishideveloper.schoolattendance.domain.repository

import com.therishideveloper.schoolattendance.data.local.dao.SchoolDao
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import javax.inject.Inject

class SchoolRepositoryImpl @Inject constructor(
    private val dao: SchoolDao
) : SchoolRepository {
    override suspend fun getSchool() = dao.getSchoolProfile()
    override suspend fun insertOrUpdate(profile: SchoolEntity) = dao.insertOrUpdate(profile)
}