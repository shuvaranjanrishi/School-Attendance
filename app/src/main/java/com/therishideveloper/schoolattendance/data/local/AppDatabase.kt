package com.therishideveloper.schoolattendance.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.therishideveloper.schoolattendance.data.local.dao.AttendanceDao
import com.therishideveloper.schoolattendance.data.local.dao.SchoolDao
import com.therishideveloper.schoolattendance.data.local.dao.StudentDao
import com.therishideveloper.schoolattendance.data.local.dao.UserDao
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.data.local.entity.UserEntity

@Database(
    entities = [
        StudentEntity::class,
        SchoolEntity::class,
        UserEntity::class,
        AttendanceEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val studentDao: StudentDao
    abstract val schoolDao: SchoolDao
    abstract val userDao: UserDao
    abstract val attendanceDao: AttendanceDao
}