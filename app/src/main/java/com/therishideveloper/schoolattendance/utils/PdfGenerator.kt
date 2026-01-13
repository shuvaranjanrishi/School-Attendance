package com.therishideveloper.schoolattendance.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.provider.SyncStateContract
import com.therishideveloper.schoolattendance.R
import com.therishideveloper.schoolattendance.data.local.SettingsManager
import com.therishideveloper.schoolattendance.data.local.entity.StudentEntity
import com.therishideveloper.schoolattendance.domain.repository.SchoolRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.graphics.scale
import com.therishideveloper.schoolattendance.data.local.entity.AttendanceEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import com.therishideveloper.schoolattendance.utils.Constants

@Singleton
class PdfGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val schoolRepository: SchoolRepository,
    private val settingsManager: SettingsManager // Dynamic language source
) {

    private suspend fun getLabel(resId: Int): String {
        val savedLanguage = settingsManager.languageFlow.first()
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(savedLanguage.code))
        return context.createConfigurationContext(config).getString(resId)
    }

    suspend fun downloadStudentPdf(student: StudentEntity): Result<File> {
        val school = schoolRepository.getSchool()
        val pdfDocument = PdfDocument()

        // PDF Page Configuration (A4 Size: 595x842)
        val pageWidth = Constants.A4_WIDTH
        val pageHeight = Constants.A4_HEIGHT
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- 1. Header Section (Logo, School Name, Address) ---
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(70, 70, false), 50f, 90f, null)
            circularBitmap.recycle()
        }

        // School Name with auto-font-size adjustment
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)

        // School Address
        paint.textSize = 14f
        paint.isFakeBoldText = false
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        canvas.drawText(schoolAddress, (pageWidth / 2).toFloat(), 80f, paint)

        // Profile Title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.color = Color.DKGRAY
        canvas.drawText(
            getLabel(R.string.pdf_student_profile),
            (pageWidth / 2).toFloat(),
            110f,
            paint
        )

        // --- 2. Profile Photo Box Section ---
        val boxWidth = 100f
        val boxHeight = 120f
        val boxLeft = 450f
        val boxTop = 70f

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.BLACK
        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f) // Dashed border
        canvas.drawRect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, paint)
        paint.pathEffect = null

        if (student.image != null) {
            val bitmap = BitmapFactory.decodeByteArray(student.image, 0, student.image.size)
            canvas.drawBitmap(
                bitmap.scale(boxWidth.toInt(), boxHeight.toInt(), false),
                boxLeft,
                boxTop,
                null
            )
        } else {
            paint.style = Paint.Style.FILL
            paint.textSize = 12f
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                getLabel(R.string.pdf_profile_photo),
                boxLeft + (boxWidth / 2),
                boxTop + 65f,
                paint
            )
        }

        // --- 3. Divider Line ---
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.BLACK
        canvas.drawLine(50f, 200f, (pageWidth - 50).toFloat(), 200f, paint)

        // --- 4. Student Data Section (Localized) ---
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT
        var yPos = 240f
        val lineSpacing = 38f

        // Internal helper function for drawing labels and values
        suspend fun drawDataRow(
            labelRes: Int,
            value: String,
            x: Float,
            y: Float,
            isBold: Boolean = false
        ) {
            val label = getLabel(labelRes)
            paint.isFakeBoldText = false
            paint.textSize = 22f
            canvas.drawText(label, x, y, paint)
            if (isBold) paint.isFakeBoldText = true
            canvas.drawText(value, x + paint.measureText(label) + 10f, y, paint)
            paint.isFakeBoldText = false
        }

        // Basic Info
        drawDataRow(R.string.pdf_label_name, student.name, 60f, yPos, true)
        yPos += lineSpacing

        val rollValue = student.rollNo.localizeDigitsAndLabels()
        val classResId = ClassTypes.fromCode(student.className).stringRes
        val classValue = getLabel(classResId).replace("Class ", "")

        drawDataRow(R.string.pdf_label_roll, rollValue, 60f, yPos, true)
        drawDataRow(R.string.pdf_label_class, classValue, (pageWidth / 2f), yPos, true)
        yPos += lineSpacing

        // Identification & Personal Details
        val idLabelRes =
            if (student.idType == IdTypes.NID.code) R.string.pdf_label_nid else R.string.pdf_label_birth_reg
        drawDataRow(idLabelRes, student.nidOrBirthReg.localizeDigitsAndLabels(), 60f, yPos)
        yPos += lineSpacing

        val personalDetails = listOf(
            R.string.pdf_label_dob to student.dateOfBirth.localizeDigitsAndLabels(),
            R.string.pdf_label_age to student.age.localizeDigitsAndLabels(),
            R.string.pdf_label_father to student.fatherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mother to student.motherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mobile to student.phone.localizeDigitsAndLabels()
        )

        for ((res, valStr) in personalDetails) {
            drawDataRow(res, valStr, 60f, yPos)
            yPos += lineSpacing
        }

        // Gender & Religion
        drawDataRow(
            R.string.pdf_label_gender,
            getLabel(GenderTypes.fromCode(student.gender).stringRes),
            60f,
            yPos
        )
        drawDataRow(
            R.string.pdf_label_religion,
            getLabel(ReligionTypes.fromCode(student.religion).stringRes),
            (pageWidth / 2f),
            yPos
        )
        yPos += lineSpacing

        // Address & Admission
        drawDataRow(
            R.string.pdf_label_blood_group,
            getLabel(BloodGroupTypes.fromCode(student.bloodGroup).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_address,
            student.address.ifBlank { getLabel(R.string.not_given) },
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_country,
            getLabel(CountryTypes.fromCode(student.country).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            R.string.pdf_label_admission_date,
            student.admissionDate?.localizeDigitsAndLabels() ?: "", 60f, yPos
        )

        // --- 5. Footer Section ---
        val footerTop = pageHeight - 60f
        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(0f, footerTop, pageWidth.toFloat(), pageHeight - 10f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(
            getLabel(R.string.pdf_footer_brand_name),
            (pageWidth / 2).toFloat(),
            footerTop + 20f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            getLabel(R.string.pdf_footer_dev_label),
            (pageWidth / 2).toFloat(),
            footerTop + 30f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            getLabel(R.string.pdf_footer_rights),
            (pageWidth / 2).toFloat(),
            footerTop + 40f,
            paint
        )

        pdfDocument.finishPage(page)

        // --- 6. Storage & Saving ---
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val finalFolder = File(downloadDir, Constants.FOLDER_PROFILES)

        return try {
            if (!finalFolder.exists()) finalFolder.mkdirs()
            val sanitizedFileName = "Std_${student.rollNo}_${student.name.replace(" ", "_")}.pdf"
            val fileToSave = File(finalFolder, sanitizedFileName)

            pdfDocument.writeTo(FileOutputStream(fileToSave))

            showDownloadNotification(
                context,
                fileToSave,
                getLabel(R.string.pdf_download_success),
                getLabel(R.string.pdf_download_desc).format(student.name),
                "application/pdf"
            )
            Result.Success(fileToSave)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "PDF Export Failed")
        } finally {
            pdfDocument.close()
        }
    }

    private fun getCircularBitmap(src: Bitmap): Bitmap {
        val size = minOf(src.width, src.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { isAntiAlias = true }
        canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(src, Rect(0, 0, size, size), Rect(0, 0, size, size), paint)
        return output
    }

    suspend fun downloadMonthlyReportPdf(
        className: String,
        monthYear: String,
        attendanceData: List<AttendanceEntity>
    ): Result<File> {
        val school = schoolRepository.getSchool()
        val pdfDocument = PdfDocument()

        // ল্যান্ডস্কেপ মোড (Landscape) হলে ভালো হয় কারণ ৩১ দিনের কলাম অনেক বড়
        // A4 Landscape: 842 (Width) x 595 (Height)
        val pageWidth = Constants.A4_WIDTH
        val pageHeight = Constants.A4_HEIGHT
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- ১. হেডার সেকশন (আপনার প্রোফাইল কোড থেকে অনুপ্রাণিত) ---
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(50, 50, false), 40f, 20f, null)
            circularBitmap.recycle()
        }

        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = 22f
        canvas.drawText(school?.name ?: "", (pageWidth / 2).toFloat(), 40f, paint)

        paint.textSize = 16f
        paint.color = Color.DKGRAY
        val reportTitle = "${getLabel(R.string.reports)}: $className ($monthYear)"
        canvas.drawText(reportTitle, (pageWidth / 2).toFloat(), 70f, paint)

        // --- ২. টেবিল কনফিগারেশন ---
        val startX = 30f
        var startY = 100f
        val nameWidth = 120f
        val dayWidth = (pageWidth - startX - nameWidth - 30f) / 31f // ডাইনামিক কলাম উইথ
        val rowHeight = 22f

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f

        // টেবিল হেডার ড্র করা
        canvas.drawRect(startX, startY, pageWidth - 30f, startY + rowHeight, paint)
        paint.style = Paint.Style.FILL
        canvas.drawText(getLabel(R.string.pdf_label_name), startX + 5, startY + 15, paint)

        for (day in 1..31) {
            val xPos = startX + nameWidth + ((day - 1) * dayWidth)
            canvas.drawText(day.toString(), xPos + 2, startY + 15, paint)
        }

        // --- ৩. ডাটা রো ড্র করা ---
        val groupedData = attendanceData.groupBy { it.studentName }
        var currentY = startY + rowHeight

        paint.isFakeBoldText = false

        groupedData.forEach { (name, records) ->
            // নতুন পাতা দরকার কি না চেক করার লজিক (ঐচ্ছিক)
            if (currentY > pageHeight - 60f) return@forEach

            paint.style = Paint.Style.STROKE
            canvas.drawRect(startX, currentY, pageWidth - 30f, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            canvas.drawText(name, startX + 5, currentY + 15, paint)

            for (day in 1..31) {
                val xPos = startX + nameWidth + ((day - 1) * dayWidth)
                val dayStr = String.format("%02d", day)
                val record = records.find { it.date.startsWith(dayStr) }

                val (statusText, color) = when (record?.status) {
                    "Present" -> "P" to "#2E7D32" // সবুজ
                    "Absent" -> "A" to "#D32F2F"  // লাল
                    else -> "-" to "#757575"      // ধূসর
                }

                paint.color = color.toColorInt()
                canvas.drawText(statusText, xPos + 4, currentY + 15, paint)
                paint.color = Color.BLACK
            }
            currentY += rowHeight
        }

        pdfDocument.finishPage(page)

        // --- ৪. সেভ এবং নোটিফিকেশন (আপনার প্রোফাইল লজিক ব্যবহার করে) ---
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val finalFolder = File(downloadDir, Constants.FOLDER_REPORTS)

        return try {
            if (!finalFolder.exists()) finalFolder.mkdirs()
            val sanitizedFileName = "Report_${className}_${monthYear.replace(" ", "_")}.pdf"
            val fileToSave = File(finalFolder, sanitizedFileName)

            pdfDocument.writeTo(FileOutputStream(fileToSave))

            showDownloadNotification(
                context,
                fileToSave,
                getLabel(R.string.pdf_download_success),
                "$className - $monthYear",
                "application/pdf"
            )
            Result.Success(fileToSave)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "PDF Export Failed")
        } finally {
            pdfDocument.close()
        }
    }

    suspend fun createStudentMonthlyDetailsReport(
        studentName: String,
        className: String,
        monthYear: String,
        rollNo: String,
        total: Int,
        present: Int,
        absent: Int,
        percent: Float,
        records: List<AttendanceEntity>,
        isSharing: Boolean = false
    ): Result<File> {
        val school = schoolRepository.getSchool()
        val pdfDocument = PdfDocument()

        // A4 Size (Portrait): 595 x 842
        val pageWidth = Constants.A4_WIDTH
        val pageHeight = Constants.A4_HEIGHT
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- ১. হেডার সেকশন ---
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        paint.color = Color.BLACK
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)

        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
        canvas.drawText(schoolAddress, (pageWidth / 2).toFloat(), 80f, paint)

        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.color = Color.DKGRAY
        val title = "${getLabel(R.string.monthly_attendance_report)}: $monthYear"
        canvas.drawText(title, (pageWidth / 2).toFloat(), 115f, paint)

        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 135f, (pageWidth - 40).toFloat(), 135f, paint)

        // --- ২. স্ট্যাটাস কার্ড (বাম পাশে প্রোফাইল, ডান পাশে প্রোগ্রেস ও স্ট্যাটাস) ---
        val cardTop = 160f
        val cardBottom = 260f
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(40f, cardTop, (pageWidth - 40).toFloat(), cardBottom, 15f, 15f, paint)

        // কার্ডের মাঝখানের ডিভাইডার
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine((pageWidth / 2).toFloat(), cardTop + 20f, (pageWidth / 2).toFloat(), cardBottom - 20f, paint)

        // --- বাম পাশ: স্টুডেন্ট ইনফো ---
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        paint.textSize = 18f
        paint.color = "#1565C0".toColorInt()
        canvas.drawText(studentName, 65f, cardTop + 35f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("শ্রেণী: $className", 65f, cardTop + 60f, paint)
        canvas.drawText("রোল: $rollNo", 65f, cardTop + 80f, paint)

        // --- ডান পাশ: পরিসংখ্যান (সরাসরি প্যারামিটার থেকে প্রাপ্ত) ---
        val rightInfoStart = (pageWidth / 2).toFloat() + 25f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText("মোট দিন: $total", rightInfoStart, cardTop + 35f, paint)
        paint.color = "#2E7D32".toColorInt()
        canvas.drawText("উপস্থিত: $present", rightInfoStart, cardTop + 55f, paint)
        paint.color = "#D32F2F".toColorInt()
        canvas.drawText("অনুপস্থিত: $absent", rightInfoStart, cardTop + 75f, paint)

        // রাইট মোস্ট প্রোগ্রেস সার্কেল
        val centerX = (pageWidth - 100).toFloat()
        val centerY = (cardTop + cardBottom) / 2
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, 30f, paint)

        paint.color = if (percent >= 80) "#2E7D32".toColorInt() else "#FBC02D".toColorInt()
        val rectF = RectF(centerX - 30f, centerY - 30f, centerX + 30f, centerY + 30f)
        canvas.drawArc(rectF, -90f, (percent * 3.6).toFloat(), false, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("${percent.toInt()}%", centerX, centerY + 5f, paint)

        // --- ৪. লিজেন্ড (Legend) ---
        val legendY = 300f
        paint.textAlign = Paint.Align.LEFT
        paint.color = "#2E7D32".toColorInt()
        canvas.drawCircle(60f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText("Present", 75f, legendY, paint)

        paint.color = "#D32F2F".toColorInt()
        canvas.drawCircle(160f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText("Absent", 175f, legendY, paint)

        paint.color = Color.LTGRAY
        canvas.drawCircle(260f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText("No Class", 275f, legendY, paint)

        // --- ৫. ক্যালেন্ডার গ্রিড ---
        val startX = 65f
        val startY = 380f
        val cellSize = 70f

        paint.color = Color.DKGRAY
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, day ->
            canvas.drawText(day, startX + (index * cellSize) + 30f, startY - 35f, paint)
        }

        val calendar = Calendar.getInstance()
        // নির্বাচিত মাস ও বছর অনুযায়ী দিন বের করা
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        var currentX = startX
        var currentY = startY
        paint.isFakeBoldText = false

        for (day in 1..maxDays) {
            val dayStr = String.format("%02d", day)
            val record = records.find { it.date.startsWith(dayStr) }

            paint.color = when (record?.status) {
                "Present" -> "#2E7D32".toColorInt()
                "Absent" -> "#D32F2F".toColorInt()
                else -> "#EEEEEE".toColorInt()
            }
            canvas.drawCircle(currentX + 30f, currentY + 10f, 25f, paint)

            paint.color = if (record != null) Color.WHITE else Color.BLACK
            canvas.drawText(day.toString(), currentX + 30f, currentY + 16f, paint)

            if (day % 7 == 0) {
                currentX = startX
                currentY += cellSize
            } else {
                currentX += cellSize
            }
        }

        // --- ৬. ফুটার ---
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.5f
        paint.color = Color.LTGRAY
        canvas.drawLine(40f, 780f, (pageWidth - 40).toFloat(), 780f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 11f
        paint.color = Color.GRAY
        paint.textAlign = Paint.Align.LEFT
        val genDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $genDate", 40f, 805f, paint)

        paint.isFakeBoldText = true
        paint.color = "#1976D2".toColorInt()
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Developed by: The Rishi Developer", (pageWidth - 40).toFloat(), 805f, paint)

        pdfDocument.finishPage(page)

        // --- ৭. সেভ লজিক (Cache/Downloads) ---
        val finalFolder = if (isSharing) {
            File(context.cacheDir, "shared_reports")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Constants.FOLDER_REPORTS)
        }

        return try {
            if (!finalFolder.exists()) finalFolder.mkdirs()
            val fileName = "Report_${studentName.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
            val fileToSave = File(finalFolder, fileName)

            pdfDocument.writeTo(FileOutputStream(fileToSave))

            if (!isSharing) {
                showDownloadNotification(context, fileToSave, "রিপোর্ট ডাউনলোড সফল হয়েছে", "$studentName - $monthYear", "application/pdf")
            }

            Result.Success(fileToSave)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to save PDF")
        } finally {
            pdfDocument.close()
        }
    }

//    suspend fun createStudentMonthlyDetailsReport(
//        studentName: String,
//        className: String,
//        monthYear: String,
//        records: List<AttendanceEntity>,
//        isSharing: Boolean = false
//    ): Result<File> {
//        val school = schoolRepository.getSchool()
//        val pdfDocument = PdfDocument()
//
//        val pageWidth = Constants.A4_WIDTH
//        val pageHeight = Constants.A4_HEIGHT
//        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
//        val page = pdfDocument.startPage(pageInfo)
//        val canvas: Canvas = page.canvas
//        val paint = Paint()
//
//        // --- ১. ডাইনামিক হেডার সেকশন (আপনার ডিজাইন অনুযায়ী) ---
//        // School Name with auto-font-size adjustment
//        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
//        paint.textAlign = Paint.Align.CENTER
//        paint.isFakeBoldText = true
//        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
//        paint.color = Color.BLACK
//        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)
//
//        // School Address
//        paint.textSize = 14f
//        paint.isFakeBoldText = false
//        paint.color = Color.GRAY
//        val schoolAddress = school?.address ?: getLabel(R.string.pdf_hint_school_address)
//        canvas.drawText(schoolAddress, (pageWidth / 2).toFloat(), 80f, paint)
//
//        // Profile Title (এখানে আমরা "মাসিক হাজিরা রিপোর্ট" দেখাচ্ছি)
//        paint.textSize = 20f
//        paint.isFakeBoldText = true
//        paint.color = Color.DKGRAY
//        val title = "${getLabel(R.string.monthly_attendance_report)}: $monthYear"
//        canvas.drawText(title, (pageWidth / 2).toFloat(), 115f, paint)
//
//        // ডিভাইডার
//        paint.color = Color.LTGRAY
//        canvas.drawLine(40f, 135f, (pageWidth - 40).toFloat(), 135f, paint)
//
//        // --- ২. স্টুডেন্ট ইনফো ---
//        paint.textAlign = Paint.Align.CENTER
//        paint.textSize = 20f
//        paint.color = "#1565C0".toColorInt()
//        canvas.drawText(studentName, (pageWidth / 2).toFloat(), 175f, paint)
//
//        paint.textSize = 14f
//        paint.color = Color.GRAY
//        canvas.drawText(
//            "রোল: ${records.firstOrNull()?.rollNo ?: "N/A"}  •  ক্লাস: $className",
//            (pageWidth / 2).toFloat(),
//            200f,
//            paint
//        )
//
//        // --- ৩. স্ট্যাটাস কার্ড (Left এ Stats, Right এ Progress) ---
//        val cardTop = 230f
//        val cardBottom = 310f
//        paint.color = Color.rgb(245, 245, 245)
//        canvas.drawRoundRect(40f, cardTop, (pageWidth - 40).toFloat(), cardBottom, 15f, 15f, paint)
//
//        val total = records.size
//        val present = records.count { it.status == "Present" }
//        val absent = total - present
//        val percent = if (total > 0) (present.toFloat() / total.toFloat()) * 100f else 0f
//
//        paint.textAlign = Paint.Align.LEFT
//        paint.color = Color.BLACK
//        paint.textSize = 14f
//        canvas.drawText("মোট দিন: $total", 70f, cardTop + 30f, paint)
//        paint.color = "#2E7D32".toColorInt()
//        canvas.drawText("উপস্থিত: $present", 70f, cardTop + 50f, paint)
//        paint.color = "#D32F2F".toColorInt()
//        canvas.drawText("অনুপস্থিত: $absent", 70f, cardTop + 70f, paint)
//
//        paint.color = Color.LTGRAY
//        canvas.drawLine(
//            (pageWidth / 2).toFloat() + 20f,
//            cardTop + 20f,
//            (pageWidth / 2).toFloat() + 20f,
//            cardBottom - 20f,
//            paint
//        )
//
//        val centerX = (pageWidth * 0.75).toFloat()
//        val centerY = (cardTop + cardBottom) / 2
//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = 8f
//        paint.color = Color.LTGRAY
//        canvas.drawCircle(centerX, centerY, 30f, paint)
//
//        paint.color = if (percent >= 80) "#2E7D32".toColorInt() else "#FBC02D".toColorInt()
//        val rectF = RectF(centerX - 30f, centerY - 30f, centerX + 30f, centerY + 30f)
//        canvas.drawArc(rectF, -90f, (percent * 3.6).toFloat(), false, paint)
//
//        paint.style = Paint.Style.FILL
//        paint.textSize = 14f
//        paint.textAlign = Paint.Align.CENTER
//        canvas.drawText("${percent.toInt()}%", centerX, centerY + 5f, paint)
//
//        // --- ৪. লিজেন্ড ---
//        val legendY = 345f
//        paint.textAlign = Paint.Align.LEFT
//        paint.color = "#2E7D32".toColorInt()
//        canvas.drawCircle(60f, legendY - 5f, 5f, paint)
//        paint.color = Color.GRAY
//        canvas.drawText("Present", 75f, legendY, paint)
//        paint.color = "#D32F2F".toColorInt()
//        canvas.drawCircle(160f, legendY - 5f, 5f, paint)
//        paint.color = Color.GRAY
//        canvas.drawText("Absent", 175f, legendY, paint)
//        paint.color = Color.LTGRAY
//        canvas.drawCircle(260f, legendY - 5f, 5f, paint)
//        paint.color = Color.GRAY
//        canvas.drawText("No Class", 275f, legendY, paint)
//
//        // --- ৫. ক্যালেন্ডার গ্রিড ---
//        val startX = 65f
//        val startY = 420f
//        val cellSize = 70f
//
//        paint.color = Color.DKGRAY
//        paint.textSize = 15f
//        paint.textAlign = Paint.Align.CENTER
//        paint.isFakeBoldText = true
//
//        listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, day ->
//            canvas.drawText(day, startX + (index * cellSize) + 30f, startY - 35f, paint)
//        }
//
//        val calendar = Calendar.getInstance()
//        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//        var currentX = startX
//        var currentY = startY
//
//        paint.isFakeBoldText = false
//
//        for (day in 1..maxDays) {
//            val dayStr = String.format("%02d", day)
//            val record = records.find { it.date.startsWith(dayStr) }
//
//            paint.color = when (record?.status) {
//                "Present" -> "#2E7D32".toColorInt()
//                "Absent" -> "#D32F2F".toColorInt()
//                else -> "#EEEEEE".toColorInt()
//            }
//            canvas.drawCircle(currentX + 30f, currentY + 10f, 25f, paint)
//
//            paint.color = if (record != null) Color.WHITE else Color.BLACK
//            canvas.drawText(day.toString(), currentX + 30f, currentY + 16f, paint)
//
//            if (day % 7 == 0) {
//                currentX = startX
//                currentY += cellSize
//            } else {
//                currentX += cellSize
//            }
//        }
//
//        // --- ৬. ফুটার ---
//        paint.style = Paint.Style.STROKE
//        paint.strokeWidth = 0.5f
//        paint.color = Color.LTGRAY
//        canvas.drawLine(40f, 780f, (pageWidth - 40).toFloat(), 780f, paint)
//
//        paint.style = Paint.Style.FILL
//        paint.textSize = 11f
//        paint.color = Color.GRAY
//        paint.textAlign = Paint.Align.LEFT
//        canvas.drawText(
//            "Generated on: ${
//                SimpleDateFormat(
//                    "dd MMM yyyy",
//                    Locale.getDefault()
//                ).format(Date())
//            }", 40f, 805f, paint
//        )
//
//        paint.isFakeBoldText = true
//        paint.color = "#1976D2".toColorInt()
//        paint.textAlign = Paint.Align.RIGHT
//        canvas.drawText(
//            "Developed by: The Rishi Developer",
//            (pageWidth - 40).toFloat(),
//            805f,
//            paint
//        )
//
//        pdfDocument.finishPage(page)
//
//        // --- ৭. সেভ লজিক ---
//        val downloadDir =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val finalFolder = if (isSharing) {
//            File(context.cacheDir, "shared_reports")
//        } else {
//            File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
//                Constants.FOLDER_REPORTS
//            )
//        }
//        return try {
//            if (!finalFolder.exists()) finalFolder.mkdirs()
//            val fileName =
//                "Report_${studentName.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
//            val fileToSave = File(finalFolder, fileName)
//            pdfDocument.writeTo(FileOutputStream(fileToSave))
//
//            if (!isSharing) {
//                showDownloadNotification(
//                    context,
//                    fileToSave,
//                    "রিপোর্ট ডাউনলোড সফল হয়েছে",
//                    "$studentName - $monthYear",
//                    "application/pdf"
//                )
//            }
//            Result.Success(fileToSave)
//        } catch (e: Exception) {
//            Result.Error(e.localizedMessage ?: "Failed to save PDF")
//        } finally {
//            pdfDocument.close()
//        }
//    }

    fun shareFile(file: File) {
        try {
            // ম্যানিফেস্টের ${applicationId}.provider অনুযায়ী
            val authority = "${context.packageName}.provider"

            val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                authority,
                file
            )

            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // ইনটেন্ট চুজারে টাইটেল দেওয়া
            val chooserIntent = android.content.Intent.createChooser(shareIntent, "Share Report via")

            // এটি অত্যন্ত গুরুত্বপূর্ণ যদি কনটেক্সট অ্যাক্টিভিটি না হয়
            chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            // লগ চেক করার জন্য
            android.util.Log.e("PdfError", "Share failed: ${e.message}")
        }
    }

}