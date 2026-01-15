package com.therishideveloper.schoolattendance.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
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
import com.therishideveloper.schoolattendance.data.local.entity.SchoolEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Singleton
class PdfGenerator @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val schoolRepository: SchoolRepository,
    private val settingsManager: SettingsManager
) {
    private data class PdfPageSetup(
        val document: PdfDocument,
        val page: PdfDocument.Page,
        val canvas: Canvas,
        val paint: Paint,
        val pageWidth: Int,
        val pageHeight: Int
    )

    private fun setupPdfPage(isLandscape: Boolean = false): PdfPageSetup {
        val pdfDocument = PdfDocument()
        val width = if (isLandscape) Constants.A4_HEIGHT else Constants.A4_WIDTH
        val height = if (isLandscape) Constants.A4_WIDTH else Constants.A4_HEIGHT

        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        return PdfPageSetup(
            document = pdfDocument,
            page = page,
            canvas = page.canvas,
            paint = Paint().apply { isAntiAlias = true },
            pageWidth = width,
            pageHeight = height
        )
    }

    suspend fun downloadStudentProfilePdf(student: StudentEntity): Result<File> {
        val school = schoolRepository.getSchool()
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = setupPdfPage(false)

        drawPortraitHeader(
            student,
            school,
            canvas,
            pageWidth,
            paint,
            getLabel(R.string.pdf_student_profile)
        )

        // --- ডাটা সেকশন শুরু ---
        var yPos = 240f
        val lineSpacing = 38f

        // বেসিক ইনফো
        drawDataRow(canvas, paint, R.string.pdf_label_name, student.name, 60f, yPos, true)
        yPos += lineSpacing

        val rollValue = student.rollNo.localizeDigitsAndLabels()
        val classValue =
            getLabel(ClassTypes.fromCode(student.classCode).stringRes).replace("Class ", "")
        drawDataRow(canvas, paint, R.string.pdf_label_roll, rollValue, 60f, yPos, true)
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_class,
            classValue,
            (pageWidth / 2f),
            yPos,
            true
        )
        yPos += lineSpacing

        // আইডি ও পার্সোনাল ডিটেইলস
        val idLabelRes =
            if (student.idType == IdTypes.NID.code) R.string.pdf_label_nid else R.string.pdf_label_birth_reg
        drawDataRow(
            canvas,
            paint,
            idLabelRes,
            student.nidOrBirthReg.localizeDigitsAndLabels(),
            60f,
            yPos
        )
        yPos += lineSpacing

        val details = listOf(
            R.string.pdf_label_dob to student.dateOfBirth.localizeDigitsAndLabels(),
            R.string.pdf_label_age to student.age.localizeDigitsAndLabels(),
            R.string.pdf_label_father to student.fatherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mother to student.motherName.ifBlank { getLabel(R.string.not_given) },
            R.string.pdf_label_mobile to student.phone.localizeDigitsAndLabels()
        )

        for ((res, value) in details) {
            drawDataRow(canvas, paint, res, value, 60f, yPos)
            yPos += lineSpacing
        }

        // জেন্ডার ও ধর্ম (একই লাইনে)
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_gender,
            getLabel(GenderTypes.fromCode(student.genderCode).stringRes),
            60f,
            yPos
        )
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_religion,
            getLabel(ReligionTypes.fromCode(student.religionCode).stringRes),
            (pageWidth / 2f),
            yPos
        )
        yPos += lineSpacing

        // অন্যান্য তথ্য
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_blood_group,
            getLabel(BloodGroupTypes.fromCode(student.bloodGroup).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_address,
            student.address.ifBlank { getLabel(R.string.not_given) },
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_country,
            getLabel(CountryTypes.fromCode(student.country).stringRes),
            60f,
            yPos
        )
        yPos += lineSpacing
        drawDataRow(
            canvas,
            paint,
            R.string.pdf_label_admission_date,
            student.admissionDate.localizeDigitsAndLabels(),
            60f,
            yPos
        )

        //draw footer
        drawProfileFooter(canvas, pageWidth, pageHeight, paint)

        pdfDocument.finishPage(page)

        val fileName = "Std_${student.rollNo}_${student.name.replace(" ", "_")}.pdf"
        val title = getLabel(R.string.pdf_download_success)
        val desc = getLabel(R.string.pdf_download_desc).format(student.name)

        return finalizePdf(pdfDocument, Constants.FOLDER_PROFILES, fileName, title, desc)
    }

    private fun finalizePdf(
        pdfDocument: PdfDocument,
        folderName: String,
        fileName: String,
        title: String,
        desc: String,
        isSharing: Boolean = false
    ): Result<File> {
        return try {
            val file = savePdfFile(pdfDocument, folderName, fileName, isSharing)

            if (!isSharing) {
                showDownloadNotification(
                    context, file, title,
                    desc, "application/pdf"
                )
            }
            Result.Success(file)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Failed to save PDF")
        } finally {
            pdfDocument.close()
        }
    }

    private suspend fun drawPortraitHeader(
        student: StudentEntity,
        school: SchoolEntity?,
        canvas: Canvas,
        pageWidth: Int,
        paint: Paint,
        title: String
    ) {
        //school logo
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(70, 70, false), 50f, 90f, null)
            circularBitmap.recycle()
        }
        //draw school name
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)
        //draw school address
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText(
            school?.address ?: getLabel(R.string.pdf_hint_school_address),
            (pageWidth / 2).toFloat(),
            80f,
            paint
        )

        //draw page title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        paint.color = Color.DKGRAY
        canvas.drawText(title, (pageWidth / 2).toFloat(), 110f, paint)

        //draw student photo
        drawPhotoBox(canvas, student.image, paint)

        //draw two line circle
        val circleRadius = 3f
        val lineStart = 40f
        val lineEnd = 200f
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas.drawCircle(lineStart, lineEnd, circleRadius, paint)
        canvas.drawCircle((pageWidth - lineStart), lineEnd, circleRadius, paint)

        //draw header divider
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        canvas.drawLine(lineStart, lineEnd, (pageWidth - lineStart), lineEnd, paint)

    }

    private suspend fun drawPhotoBox(canvas: Canvas, imageBytes: ByteArray?, paint: Paint) {
        val boxWidth = 100f;
        val boxHeight = 120f;
        val boxLeft = 450f;
        val boxTop = 70f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.BLACK
        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        canvas.drawRect(boxLeft, boxTop, boxLeft + boxWidth, boxTop + boxHeight, paint)
        paint.pathEffect = null

        imageBytes?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            canvas.drawBitmap(
                bitmap.scale(boxWidth.toInt(), boxHeight.toInt(), false),
                boxLeft,
                boxTop,
                null
            )
        } ?: run {
            paint.style = Paint.Style.FILL
            paint.textSize = 12f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                getLabel(R.string.pdf_profile_photo),
                boxLeft + 50f,
                boxTop + 65f,
                paint
            )
        }
    }

    private suspend fun drawDataRow(
        canvas: Canvas,
        paint: Paint,
        labelRes: Int,
        value: String,
        x: Float,
        y: Float,
        isBold: Boolean = false
    ) {
        val label = "${getLabel(labelRes)}: "
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText(label, x, y, paint)
        if (isBold) paint.isFakeBoldText = true
        canvas.drawText(value, x + paint.measureText(label) + 10f, y, paint)
        paint.isFakeBoldText = false
    }

    private suspend fun drawProfileFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        paint: Paint
    ) {
        val footerTop = pageHeight - 60f
        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(0f, footerTop, pageWidth.toFloat(), pageHeight - 10f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(
            getLabel(R.string.dev_name),
            (pageWidth / 2).toFloat(),
            footerTop + 20f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.LTGRAY
        canvas.drawText(
            getLabel(R.string.label_developer),
            (pageWidth / 2).toFloat(),
            footerTop + 30f,
            paint
        )
        canvas.drawText(
            getLabel(R.string.dev_copy_rights),
            (pageWidth / 2).toFloat(),
            footerTop + 40f,
            paint
        )
    }

    private suspend fun getLabel(resId: Int): String {
        val savedLanguage = settingsManager.languageFlow.first()
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale(savedLanguage.code))
        return context.createConfigurationContext(config).getString(resId)
    }

    private suspend fun drawCommonHeader(
        canvas: Canvas,
        school: SchoolEntity?,
        pageWidth: Int,
        paint: Paint,
        title: String
    ) {
        //draw school logo
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(50, 50, false), 40f, 20f, null)
            circularBitmap.recycle()
        }
        //draw school name
        val schoolName = school?.name ?: getLabel(R.string.pdf_hint_school_name)
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        paint.color = Color.BLACK
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)

        //draw address
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.GRAY
        canvas.drawText(
            school?.address ?: getLabel(R.string.pdf_hint_school_address),
            (pageWidth / 2).toFloat(),
            80f,
            paint
        )

        //draw page title
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.color = Color.DKGRAY
        canvas.drawText(title, (pageWidth / 2).toFloat(), 110f, paint)

        //draw two line circle
        val circleRadius = 3f
        val lineStart = 40f
        val lineEnd = 140f
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas.drawCircle(lineStart, lineEnd, circleRadius, paint)
        canvas.drawCircle((pageWidth - lineStart), lineEnd, circleRadius, paint)

        //draw divider line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        paint.pathEffect = null
        canvas.drawLine(lineStart, lineEnd, (pageWidth - lineStart), lineEnd, paint)

        //make paint default
        paint.style = Paint.Style.FILL
        paint.isFakeBoldText = false
    }

    private suspend fun drawCommonFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        paint: Paint
    ) {
        //draw two line circle
        val circleRadius = 3f
        val lineStart = 40f
        val lineEnd = pageHeight - 50f
        paint.style = Paint.Style.FILL
        paint.color = Color.LTGRAY
        canvas.drawCircle(lineStart, lineEnd, circleRadius, paint)
        canvas.drawCircle((pageWidth - lineStart), lineEnd, circleRadius, paint)
        //draw footer divider line
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.LTGRAY
        paint.pathEffect = null
        canvas.drawLine(lineStart, lineEnd, (pageWidth - lineStart), lineEnd, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 11f
        paint.color = Color.GRAY
        paint.textAlign = Paint.Align.LEFT
        val genDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            .localizeDigitsAndLabels()
        canvas.drawText(
            "${getLabel(R.string.generated_on)}: $genDate",
            40f,
            pageHeight - 30f,
            paint
        )

        paint.isFakeBoldText = true
        paint.color = "#1976D2".toColorInt()
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            "${getLabel(R.string.label_developer)}: ${getLabel(R.string.dev_name)}",
            (pageWidth - 40f),
            pageHeight - 30f,
            paint
        )
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

    suspend fun generateMonthlyAttendanceReport(
        classCode: String,
        monthYear: String,
        attendanceData: List<AttendanceEntity>
    ): Result<File> {
        val school = schoolRepository.getSchool()
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = setupPdfPage(true)

        // ২. হেডার
        val className = getLabel(ClassTypes.fromCode(classCode).stringRes).replace("Class ", "")
        drawCommonHeader(
            canvas,
            school,
            pageWidth,
            paint,
            "${getLabel(R.string.label_report)}: $className ($monthYear)"
        )

        // ৩. টেবিল ডাইমেনশন ও কালার
        val startX = 30f
        val startY = 160f
        val nameWidth = 140f
        val totalTableWidth = pageWidth - 60f
        val dayWidth = (totalTableWidth - nameWidth) / 31f
        val rowHeight = 25f
        val borderColor = "#CCCCCC".toColorInt() // সব বর্ডার ও ডিভাইডারের জন্য একই কালার

        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(startX, startY, startX + totalTableWidth, startY + rowHeight, paint)

        // এবার হেডারের মেইন বর্ডার
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = borderColor
        canvas.drawRect(startX, startY, startX + totalTableWidth, startY + rowHeight, paint)

        // হেডার টেক্সট (নাম এবং তারিখ)
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(getLabel(R.string.label_name), startX + 8, startY + 17, paint)

        paint.textAlign = Paint.Align.CENTER
        for (day in 1..31) {
            val xPos = startX + nameWidth + ((day - 1) * dayWidth) + (dayWidth / 2)
            val dividerX = startX + nameWidth + ((day - 1) * dayWidth)

            canvas.drawText(day.toString().localizeDigitsAndLabels(), xPos, startY + 17, paint)

            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            canvas.drawLine(dividerX, startY, dividerX, startY + rowHeight, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
        }

        var currentY = startY + rowHeight
        val groupedData = attendanceData.groupBy { it.studentName }

        groupedData.forEach { (name, records) ->
            if (currentY > pageHeight - 80f) return@forEach

            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            canvas.drawRect(startX, currentY, startX + totalTableWidth, currentY + rowHeight, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.BLACK
            paint.isFakeBoldText = false
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(name, startX + 8, currentY + 17, paint)

            paint.textAlign = Paint.Align.CENTER
            for (day in 1..31) {
                val xPos = startX + nameWidth + ((day - 1) * dayWidth) + (dayWidth / 2)
                val dividerX = startX + nameWidth + ((day - 1) * dayWidth)
                val dayStr = String.format("%02d", day)
                val record = records.find { it.date.startsWith(dayStr) }

                val (status, color) = when (record?.status) {
                    "Present" -> "P" to "#2E7D32"
                    "Absent" -> "A" to "#D32F2F"
                    else -> "-" to "#9E9E9E"
                }

                // কলাম ডিভাইডার লাইন (একই borderColor)
                paint.style = Paint.Style.STROKE
                paint.color = borderColor
                canvas.drawLine(dividerX, currentY, dividerX, currentY + rowHeight, paint)

                // স্ট্যাটাস টেক্সট (P/A/-)
                paint.style = Paint.Style.FILL
                paint.color = color.toColorInt()
                canvas.drawText(status, xPos, currentY + 17, paint)
            }
            currentY += rowHeight
        }

        //draw footer
        drawCommonFooter(canvas, pageWidth, pageHeight, paint)

        pdfDocument.finishPage(page)

        val fileName =
            "Monthly_Report_${className.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
        val title = getLabel(R.string.pdf_download_success)
        val desc = "$className - $monthYear"

        return finalizePdf(pdfDocument, Constants.FOLDER_PROFILES, fileName, title, desc)
    }

    suspend fun generateStudentMonthlyDetailsReport(
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
        val (pdfDocument, page, canvas, paint, pageWidth, pageHeight) = setupPdfPage(false)
        val title = "${getLabel(R.string.monthly_attendance_report)}: $monthYear"

        drawCommonHeader(canvas, school, pageWidth, paint, title)

        canvas.drawRoundRect(40f, 165f, (pageWidth - 40).toFloat(), 265f, 15f, 15f, paint)

        val cardTop = 165f
        val cardBottom = 265f
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(40f, cardTop, (pageWidth - 40).toFloat(), cardBottom, 15f, 15f, paint)

        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(
            (pageWidth / 2).toFloat(),
            cardTop + 20f,
            (pageWidth / 2).toFloat(),
            cardBottom - 20f,
            paint
        )

        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        paint.textSize = 18f
        paint.color = "#1565C0".toColorInt() // ব্লু কালার
        canvas.drawText(studentName, 65f, cardTop + 35f, paint)

        paint.textSize = 13f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("${getLabel(R.string.label_class)}: $className", 65f, cardTop + 60f, paint)
        canvas.drawText(
            "${getLabel(R.string.label_roll)}: ${rollNo.localizeDigitsAndLabels()}",
            65f,
            cardTop + 80f,
            paint
        )

        val rightInfoStart = (pageWidth / 2).toFloat() + 25f
        paint.textSize = 12f
        paint.color = Color.BLACK
        canvas.drawText(
            "${getLabel(R.string.total_days)}: ${
                total.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 35f, paint
        )

        paint.color = "#2E7D32".toColorInt() // গ্রিন
        canvas.drawText(
            "${getLabel(R.string.present)}: ${
                present.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 55f, paint
        )

        paint.color = "#D32F2F".toColorInt() // রেড
        canvas.drawText(
            "${getLabel(R.string.absent)}: ${
                absent.toString().localizeDigitsAndLabels()
            }", rightInfoStart, cardTop + 75f, paint
        )

        // --- প্রোগ্রেস সার্কেল (আর্ক) ---
        val centerX = (pageWidth - 100).toFloat()
        val centerY = (cardTop + cardBottom) / 2
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.LTGRAY
        canvas.drawCircle(centerX, centerY, 30f, paint) // ব্যাকগ্রাউন্ড সার্কেল

        paint.color = if (percent >= 80) "#2E7D32".toColorInt() else "#FBC02D".toColorInt()
        val rectF = RectF(centerX - 30f, centerY - 30f, centerX + 30f, centerY + 30f)
        canvas.drawArc(rectF, -90f, (percent * 3.6).toFloat(), false, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 12f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "${percent.toInt()}%".localizeDigitsAndLabels(),
            centerX,
            centerY + 5f,
            paint
        )

        drawAttendanceLegend(canvas, paint, 300f)
        drawCalendarGrid(canvas, paint, 65f, 380f, records)
        drawCommonFooter(canvas, pageWidth, pageHeight, paint)

        pdfDocument.finishPage(page)

        val folder = if (isSharing) "shared_reports" else Constants.FOLDER_REPORTS
        val fileName = "Report_${studentName.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
        val desc = getLabel(R.string.pdf_download_desc).format(studentName)

        return finalizePdf(
            pdfDocument = pdfDocument,
            folderName = folder,
            fileName = fileName,
            title = title,
            desc = desc,
            isSharing = isSharing
        )
    }

    private suspend fun drawAttendanceLegend(canvas: Canvas, paint: Paint, legendY: Float) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f

        // Present
        paint.color = "#2E7D32".toColorInt()
        canvas.drawCircle(60f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.present), 75f, legendY, paint)

        // Absent
        paint.color = "#D32F2F".toColorInt()
        canvas.drawCircle(160f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.absent), 175f, legendY, paint)

        // No Class
        paint.color = Color.LTGRAY
        canvas.drawCircle(260f, legendY - 5f, 5f, paint)
        paint.color = Color.GRAY
        canvas.drawText(getLabel(R.string.no_class), 275f, legendY, paint)
    }

    private fun drawCalendarGrid(
        canvas: Canvas,
        paint: Paint,
        startX: Float,
        startY: Float,
        records: List<AttendanceEntity>
    ) {
        val cellSize = 70f
        paint.color = Color.DKGRAY
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        // বারের নাম (Sun, Mon...)
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        daysOfWeek.forEachIndexed { index, day ->
            canvas.drawText(
                day.localizeDigitsAndLabels(),
                startX + (index * cellSize) + 30f,
                startY - 35f,
                paint
            )
        }

        // ক্যালেন্ডার ডেইস লুপ
        val calendar = Calendar.getInstance()
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var currentX = startX
        var currentY = startY
        paint.isFakeBoldText = false

        for (day in 1..maxDays) {
            val dayStr = String.format("%02d", day)
            val record = records.find { it.date.startsWith(dayStr) }

            // স্ট্যাটাস অনুযায়ী সার্কেল কালার
            paint.color = when (record?.status) {
                "Present" -> "#2E7D32".toColorInt()
                "Absent" -> "#D32F2F".toColorInt()
                else -> "#EEEEEE".toColorInt()
            }
            canvas.drawCircle(currentX + 30f, currentY + 10f, 25f, paint)

            // দিনের সংখ্যা (সাদা বা কালো)
            paint.color = if (record != null) Color.WHITE else Color.BLACK
            canvas.drawText(
                day.toString().localizeDigitsAndLabels(),
                currentX + 30f,
                currentY + 16f,
                paint
            )

            // ৭ দিন পর পর নতুন লাইন
            if (day % 7 == 0) {
                currentX = startX
                currentY += cellSize
            } else {
                currentX += cellSize
            }
        }
    }

    private fun savePdfFile(
        pdf: PdfDocument,
        folder: String,
        fileName: String,
        isSharing: Boolean
    ): File {
        val dir =
            if (isSharing) context.cacheDir else Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
        val finalFolder = File(dir, folder); if (!finalFolder.exists()) finalFolder.mkdirs()
        val file = File(finalFolder, fileName.replace(" ", "_"))
        pdf.writeTo(FileOutputStream(file)); pdf.close()
        return file
    }

    fun sharePdfFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

}

//712