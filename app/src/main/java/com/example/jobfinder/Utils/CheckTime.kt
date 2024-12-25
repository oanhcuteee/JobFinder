package com.example.jobfinder.Utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*

object CheckTime {

    //kiểm tra xem giờ điểm danh có bắt đàu trước giờ bắt đầu không
    fun checkTimeBefore(checkInTime: String, startTime: String): Boolean {
        if (checkInTime.isEmpty() || startTime.isEmpty()) {
            return false
        } else {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            return try {
                // Parse checkInTime và startTime để so sánh
                val checkInTimeParsed = timeFormat.parse(checkInTime)
                val startTimeParsed = timeFormat.parse(startTime)

                if (checkInTimeParsed != null && startTimeParsed != null) {
                    // So sánh checkInTime và startTime
                    checkInTimeParsed.before(startTimeParsed) || checkInTimeParsed == startTimeParsed
                } else {
                    false
                }
            } catch (e: Exception) {
                // Xử lý nếu có lỗi khi parse
                e.printStackTrace()
                false
            }
        }
    }

    fun calculateMinuteDiff(startTime: String, endTime: String): Int {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return try {
            // Phân tích startTime và endTime với định dạng "HH:mm"
            val startFullDate = timeFormat.parse(startTime)!!
            val endFullDate = timeFormat.parse(endTime)!!

            // Tính sự chênh lệch ở dạng milliseconds
            val diffInMillis = endFullDate.time - startFullDate.time

            // Chuyển đổi từ milliseconds sang phút
            val diffInMinutes = (diffInMillis / (1000 * 60)).toInt()

            diffInMinutes
        } catch (e: Exception) {
            e.printStackTrace()
            -999
        }
    }

    fun isDateAfter(today: String, endTime: String): Boolean {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val todayDate = sdf.parse(today)
            val endTimeDate = sdf.parse(endTime)
            if (todayDate != null && endTimeDate != null) {
                !todayDate.before(endTimeDate)
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun areDatesEqual(dateStr1: String, dateStr2: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val date1 = dateFormat.parse(dateStr1)
        val date2 = dateFormat.parse(dateStr2)
        return date1 == date2
    }

}