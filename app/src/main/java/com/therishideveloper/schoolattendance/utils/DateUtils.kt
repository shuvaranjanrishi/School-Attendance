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

    fun getFormattedDate(month: String, year: String): String {
        return try {
            // মাস এবং বছরকে একটি ডেট ফরম্যাটে নিয়ে আসা (যেমন: "01-2026")
            val sdfInput = SimpleDateFormat("MM-yyyy", Locale.US)
            val date = sdfInput.parse("$month-$year")

            // এবার সেটাকে সুন্দর করে "January 2026" বা "জানুয়ারি ২০২৬" এ রূপান্তর করা
            // Locale.getDefault() দিলে ইউজারের ভাষা অনুযায়ী মাস আসবে
            val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            date?.let { sdfOutput.format(it) } ?: "$month/$year"
        } catch (e: Exception) {
            "$month/$year" // এরর হলে ব্যাকআপ হিসেবে সংখ্যাই দেখাবে
        }
    }
}