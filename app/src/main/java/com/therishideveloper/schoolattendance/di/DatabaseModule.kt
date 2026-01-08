package com.therishideveloper.schoolattendance.di

import android.content.Context
import androidx.room.Room
import com.therishideveloper.schoolattendance.data.local.AppDatabase // নতুন নাম ইম্পোর্ট করুন
import com.therishideveloper.schoolattendance.data.local.dao.AttendanceDao
import com.therishideveloper.schoolattendance.data.local.dao.SchoolDao
import com.therishideveloper.schoolattendance.data.local.dao.StudentDao
import com.therishideveloper.schoolattendance.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    const val DB_NAME = "school_attendance_db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        ).build()
    }

    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao {
        return db.studentDao
    }

    @Provides
    fun provideSchoolDao(db: AppDatabase): SchoolDao {
        return db.schoolDao
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao {
        return db.userDao
    }

    @Provides
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao {
        return db.attendanceDao
    }

}