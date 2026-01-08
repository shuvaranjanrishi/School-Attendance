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

//    fun getTodayDate(): String {
//        return SimpleDateFormat(DB_DATE_FORMAT, Locale.US).format(Date())
//    }

    // মাসের জন্য সঠিক ফরম্যাট: "-MM-yyyy" (যেমন: "-01-2026")
    fun getCurrentMonth(): String = SimpleDateFormat("-MM-yyyy", Locale.getDefault()).format(Date())
    // বছরের জন্য সঠিক ফরম্যাট: "-yyyy" (যেমন: "-2026")
    fun getCurrentYear(): String = SimpleDateFormat("-yyyy", Locale.getDefault()).format(Date())

    // ১. আজকের তারিখের স্ট্রিং নেওয়ার সময় Locale.getDefault() দিন
    fun getTodayDate(): String {
        // এখানে Locale.US এর বদলে Locale.getDefault() ব্যবহার করুন
        return SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault()).format(Date())
    }

    // ২. ডিসপ্লে করার সময়ও Locale.getDefault() নিশ্চিত করুন
    fun getDisplayDate(dateStr: String): String {
        return try {
            // Locale.US এর বদলে Locale.getDefault() দিন, এটাই ম্যাজিক করবে
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(dateStr) ?: Date()
            SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
//    fun getDisplayDate(dateStr: String): String {
//        return try {
//            val date = SimpleDateFormat(DB_DATE_FORMAT, Locale.US).parse(dateStr) ?: Date()
//            SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.getDefault()).format(date)
//        } catch (e: Exception) {
//            dateStr
//        }
//    }

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