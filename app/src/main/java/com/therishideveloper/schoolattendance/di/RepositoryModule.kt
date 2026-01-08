package com.therishideveloper.schoolattendance.di

import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepository
import com.therishideveloper.schoolattendance.domain.repository.AttendanceRepositoryImpl
import com.therishideveloper.schoolattendance.domain.repository.UserRepositoryImpl
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepository
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepositoryImpl
import com.therishideveloper.schoolattendance.domain.repository.StudentRepository
import com.therishideveloper.schoolattendance.domain.repository.StudentRepositoryImpl
import com.therishideveloper.schoolattendance.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSchoolRepository(impl: SchoolRepositoryImpl): SchoolRepository

    @Binds
    @Singleton
    abstract fun bindStudentRepository(impl: StudentRepositoryImpl): StudentRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(impl: AttendanceRepositoryImpl): AttendanceRepository
}