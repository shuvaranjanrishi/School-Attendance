package com.therishideveloper.schoolattendance.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_school")
data class SchoolEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val address: String,
    val logo: ByteArray? = null,
    val banner: ByteArray? = null,
)