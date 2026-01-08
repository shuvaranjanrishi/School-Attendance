package com.therishideveloper.schoolattendance.utils

sealed class Result<out T> {

    // যখন ডাটা লোড হচ্ছে
    data object Loading : Result<Nothing>()

    // যখন ডাটা সফলভাবে পাওয়া গেছে
    data class Success<out T>(val data: T) : Result<T>()

    // যখন কোনো এরর বা সমস্যা হয়েছে
    data class Error(val message: String) : Result<Nothing>()
}