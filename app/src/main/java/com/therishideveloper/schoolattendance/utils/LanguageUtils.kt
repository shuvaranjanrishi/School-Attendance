package com.therishideveloper.schoolattendance.utils

import java.util.Locale

fun String.localizeDigitsAndLabels(): String {
    val currentLang = Locale.getDefault().language

    val englishDigits = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bengaliDigits = listOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val hindiDigits = listOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९')

    val result = this.map { char ->
        when (currentLang) {
            "bn" -> if (char in englishDigits) bengaliDigits[char - '0'] else char
            "hi" -> if (char in englishDigits) hindiDigits[char - '0'] else char
            else -> char
        }
    }.joinToString("")

    return when (currentLang) {
        "bn" -> {
            result.replace("Year", "বছর")
                .replace("Month", "মাস")
                .replace("Day", "দিন")
                .replace("Years", "বছর")
                .replace("Months", "মাস")
                .replace("Days", "দিন")
        }
        "hi" -> {
            result.replace("Year", "वर्ष")
                .replace("Month", "माह")
                .replace("Day", "दिन")
                .replace("Years", "वर्ष")
                .replace("Months", "माह")
                .replace("Days", "दिन")
        }
        else -> result
    }
}