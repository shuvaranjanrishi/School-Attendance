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

object PdfHelper {

    data class PdfPageSetup(
        val document: PdfDocument,
        val page: PdfDocument.Page,
        val canvas: Canvas,
        val paint: Paint,
        val pageWidth: Int,
        val pageHeight: Int
    )

    fun setupPdfPage(isLandscape: Boolean = false): PdfPageSetup {
        val width = if (isLandscape) Constants.A4_HEIGHT else Constants.A4_WIDTH
        val height = if (isLandscape) Constants.A4_WIDTH else Constants.A4_HEIGHT
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        return PdfPageSetup(
            pdfDocument,
            page,
            page.canvas,
            Paint().apply { isAntiAlias = true },
            width,
            height
        )
    }

    fun drawPhotoBox(canvas: Canvas, imageBytes: ByteArray?, paint: Paint, labelPhoto: String) {
        val boxWidth = 100f;
        val boxHeight = 120f;
        val boxLeft = 450f;
        val boxTop = 70f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.GRAY
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
            paint.color = Color.GRAY
            paint.textSize = 12f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(labelPhoto, boxLeft + 50f, boxTop + 65f, paint)
        }
    }

    fun drawDataRow(
        canvas: Canvas,
        paint: Paint,
        label: String,
        value: String,
        x: Float,
        y: Float,
        isBold: Boolean = false
    ) {
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText(label, x, y, paint)
        val offset = paint.measureText("$label: ")
        if (isBold) paint.isFakeBoldText = true
        canvas.drawText(value, x + offset + 10f, y, paint)
        paint.isFakeBoldText = false
    }

    fun drawCommonFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        paint: Paint,
        devName: String,
        devLabel: String,
        genLabel: String,
        genDate: String
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
        canvas.drawText(
            "$genLabel: $genDate",
            40f,
            pageHeight - 30f,
            paint
        )

        paint.isFakeBoldText = true
        paint.color = "#1976D2".toColorInt()
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$devLabel: $devName", pageWidth - 40f, pageHeight - 30f, paint)

    }


    fun drawProfileFooter(
        canvas: Canvas,
        pageWidth: Int,
        pageHeight: Int,
        paint: Paint,
        devName: String,
        devAuthorName: String,
        devCopyRights: String
    ) {
        val footerTop = pageHeight - 60f
        paint.style = Paint.Style.FILL
        paint.color = "#F5F5F5".toColorInt()
        canvas.drawRect(0f, footerTop, pageWidth.toFloat(), pageHeight - 10f, paint)

        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.BLACK
        paint.textSize = 12f
        canvas.drawText(
            devName,
            (pageWidth / 2).toFloat(),
            footerTop + 20f,
            paint
        )
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText(
            devAuthorName,
            (pageWidth / 2).toFloat(),
            footerTop + 30f,
            paint
        )
        canvas.drawText(
            devCopyRights,
            (pageWidth / 2).toFloat(),
            footerTop + 40f,
            paint
        )
    }

    fun drawCommonHeader(
        canvas: Canvas,
        school: SchoolEntity?,
        pageWidth: Int,
        paint: Paint,
        title: String,
        schoolName: String,
        schoolAddress: String,

    ) {
        //draw school logo
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(50, 50, false), 40f, 20f, null)
            circularBitmap.recycle()
        }
        //draw school name
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
            schoolAddress,
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

    fun drawPortraitHeader(
        student: StudentEntity,
        school: SchoolEntity?,
        canvas: Canvas,
        pageWidth: Int,
        paint: Paint,
        schoolName: String,
        schoolAddress: String,
        title: String,
        studentProfile: String
    ) {
        //school logo
        school?.logo?.let { bytes ->
            val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            val circularBitmap = getCircularBitmap(originalBitmap)
            canvas.drawBitmap(circularBitmap.scale(70, 70, false), 50f, 90f, null)
            circularBitmap.recycle()
        }
        //draw school name
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        paint.textSize = if (paint.measureText(schoolName) > 280f) 22f else 28f
        canvas.drawText(schoolName, (pageWidth / 2).toFloat(), 55f, paint)
        //draw school address
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText(
            schoolAddress,
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
        drawPhotoBox(
            canvas,
            student.image,
            paint,
            studentProfile
        )

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

    fun savePdfFile(
        context: Context,
        pdf: PdfDocument,
        folder: String,
        fileName: String,
        isSharing: Boolean
    ): File {
        val dir =
            if (isSharing) context.cacheDir else Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
        val finalFolder = File(dir, folder).apply { if (!exists()) mkdirs() }
        val file = File(finalFolder, fileName.replace(" ", "_"))
        pdf.writeTo(FileOutputStream(file))
        return file
    }

    fun drawCalendarGrid(
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

    fun finalizePdf(
        context: Context,
        pdfDocument: PdfDocument,
        folderName: String,
        fileName: String,
        title: String,
        desc: String,
        isSharing: Boolean = false
    ): Result<File> {
        return try {
            val file = savePdfFile(context, pdfDocument, folderName, fileName, isSharing)

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
}