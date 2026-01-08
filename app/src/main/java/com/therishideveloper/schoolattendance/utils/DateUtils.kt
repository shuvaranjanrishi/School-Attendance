package com.therishideveloper.schoolattendance.utils

import java.time.LocalDate
import java.time.Period
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DB_DATE_FORMAT = "dd-MM-yyyy"

    fun calculateDuration(selectedDate: LocalDate): String {
        val currentDate = LocalDate.now()
        val period = Period.between(selectedDate, currentDate)
        return "${period.years} Year ${period.months} Month ${period.days} Day"
    }

    fun formatDateToString(date: LocalDate): String {
        return "${date.dayOfMonth}-${date.monthValue}-${date.year}"
    }

    fun getTodayDate(): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.US).format(Date())
    }

    fun getDisplayDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat(DB_DATE_FORMAT, Locale.US).parse(dateStr) ?: Date()
            SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun dateToMillis(dateStr: String): Long {
        return try {
            SimpleDateFormat(DB_DATE_FORMAT, Locale.US).parse(dateStr)?.time
                ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun millisToDate(millis: Long): String {
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.US).format(Date(millis))
    }
}