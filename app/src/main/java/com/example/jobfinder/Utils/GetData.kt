package com.example.jobfinder.Utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.jobfinder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Calendar
import java.util.Date

// Cách dùng getUserRole:
// Khai báo giá trị mặc định cho 1 biến global ở Activity cần check :  private var userRole: String = ""
// Gọi hàm và gán giá trị thu được từ callback cho biến vừa khai báo và sử dụng:
//      GetData.getUserRole { role ->
//          role?.let {
//              userRole = it
//          // biến userRole sẽ mang kết quả thu được và chỉ sử dụng được trong callback này (tham khảo home)
//          }
//      }

object GetData {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        return dateFormat.format(calendar.time)
    }

    fun convertStringToDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) {
            return null
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        try {
            return dateFormat.parse(dateString)
        } catch (e: ParseException) {
            // Xử lý nếu chuỗi ngày không đúng định dạng
            e.printStackTrace()
        }
        return null
    }

    fun convertStringToDate2(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) {
            return null
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        try {
            return dateFormat.parse(dateString)
        } catch (e: ParseException) {
            // Xử lý nếu chuỗi ngày không đúng định dạng
            e.printStackTrace()
        }
        return null
    }

    //kiểm tra ngày B có trước ngày A không
    fun compareDates(dateA: String, dateB: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        if(dateA.isNotEmpty() && dateB.isNotEmpty()) {
            // Chuyển đổi chuỗi ngày thành đối tượng Date
            val dateObjA = dateFormat.parse(dateA)
            val dateObjB = dateFormat.parse(dateB)

            // So sánh ngày
            if (dateObjB != null && dateObjA != null) {
                return !dateObjB.before(dateObjA)
            }
        }
        return false
    }

    //đếm số ngày từ ngày A đến ngày B
    fun countDaysBetweenDates(dateA: String, dateB: String): Int {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        var result = -1 // Khởi tạo giá trị mặc định nếu có lỗi xảy ra

        try {
            val startDate = dateFormat.parse(dateA)
            val endDate = dateFormat.parse(dateB)

            val diffInMillis = endDate.time - startDate.time
            result = (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return result // Di chuyển lệnh return ra khỏi khối try-catch
    }

    // lấy ngày theo dang dd/MM/yyyy từ String date
    fun getDateFromString(dateTimeString: String): String {
        val parts = dateTimeString.split(" ")
        if (parts.isNotEmpty()) {
            return parts[0] // Trả về phần tử đầu tiên, chứa ngày tháng năm
        }
        return ""
    }

    fun getTimeFromString(date: String): String {
        return try {
            // Định dạng ban đầu
            val fullDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            // Định dạng giờ và phút
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            // Phân tích chuỗi ngày giờ thành đối tượng Date
            val parsedDate = fullDateFormat.parse(date)

            // Chuyển đối tượng Date thành chuỗi giờ và phút
            timeFormat.format(parsedDate!!)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    // chuyển dạng từ dd/MM/yyyy -> dd-MM-yyyy
    fun formatDateForFirebase(date: String): String {
        return date.replace("/", "-")
    }
    // Chuyển chuỗi ngày tháng từ String thành kiểu Date - Lấy dd/MM/yyyy
    fun convertStringToDATE(dateTimeString: String): Date? {
        val parts = dateTimeString.split(" ")
        if (parts.isNotEmpty()) {
            val dateString = parts[0]
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.parse(dateString)
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateMonthYearForFirebase(date:String):String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDate = LocalDate.parse(date, formatter)
        if (formatedDate!= null) {
            val result = "${formatedDate.monthValue}-${formatedDate.year}"
            return result
        }
        return ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateMonthYear(date:String):String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDate = LocalDate.parse(date, formatter)
        if (formatedDate!= null) {
            val result = "${formatedDate.monthValue}/${formatedDate.year}"
            return result
        }
        return ""
    }


    @SuppressLint("DefaultLocale")
    fun formatIntToTime(hours: Int): String {
        val totalMinutes = hours * 60
        val hoursPart = totalMinutes / 60
        val minutesPart = totalMinutes % 60
        return String.format("%02d:%02d", hoursPart, minutesPart)
    }


    fun multiplyStrings(string1: String, string2: String): String {
        // Chuyển đổi chuỗi thành số float
        val number1 = string1.toFloatOrNull() ?: 0f
        val number2 = string2.toFloatOrNull() ?: 0f

        val result = number1 * number2

        return result.toString()
    }

    //kiểm tra xem A có lớn hơn B không
    fun compareFloatStrings(strA: String, strB: String): Boolean {
        val floatA = strA.toFloatOrNull()
        val floatB = strB.toFloatOrNull()

        if (floatA != null && floatB != null) {
            return floatA >= floatB
        }
        return false
    }

    fun setStatus(startTime: String, endTime: String, empAmount: String, recruitedEmp: String): String {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val startDate = sdf.parse(startTime)
            val endDate = sdf.parse(endTime)
            val today = sdf.parse(currentDate)

            val empAmountInt = empAmount.toIntOrNull()
            val recruitedEmpInt = recruitedEmp.toIntOrNull()

            if (empAmountInt != null && recruitedEmpInt != null && today!= null) {

                return when {
                    today.after(endDate) -> "closed"
                    (!today.before(startDate) && !today.after(endDate)) && recruitedEmpInt != 0-> "working"
                    recruitedEmpInt >= empAmountInt-> "closed2"
                    today.before(startDate) -> "recruiting"
                    else -> "closed"
                }
            }

            return "closed"

        }catch (e: Exception) {
            e.printStackTrace()
            return "null"
        }
    }


    fun isTimeBeforeOneHour(timeA: String, timeB: String): Boolean {
        if(timeA.isNotEmpty() && timeB.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                sdf.isLenient = false

                // Parse thời gian từ chuỗi
                val dateA = sdf.parse(timeA)
                val dateB = sdf.parse(timeB)

                // Sử dụng Calendar để tính toán chênh lệch thời gian
                val calendarA = Calendar.getInstance().apply { time = dateA }
                val calendarB = Calendar.getInstance().apply { time = dateB }

                // Xóa thông tin liên quan đến ngày
                calendarA.set(Calendar.YEAR, 0)
                calendarA.set(Calendar.MONTH, 0)
                calendarA.set(Calendar.DAY_OF_MONTH, 0)

                calendarB.set(Calendar.YEAR, 0)
                calendarB.set(Calendar.MONTH, 0)
                calendarB.set(Calendar.DAY_OF_MONTH, 0)

                // Chênh lệch thời gian tính bằng millisecond
                val timeDifference = calendarB.timeInMillis - calendarA.timeInMillis

                // Chuyển đổi 1 giờ thành millisecond
                val oneHourInMillis = 3600000 // 1 giờ = 60 phút * 60 giây * 1000 milliseconds

                // Kiểm tra xem thời gian chênh lệch có lớn hơn hoặc bằng 1 tiếng không
                return timeDifference >= oneHourInMillis
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun calculateHourDifference(timeA: String, timeB: String): Float {
        // Split the time strings into hours and minutes
        val (hoursA, minutesA) = timeA.split(":").map { it.toInt() }
        val (hoursB, minutesB) = timeB.split(":").map { it.toInt() }

        // Calculate the total minutes from the start of the day for both times
        val totalMinutesA = hoursA * 60 + minutesA
        val totalMinutesB = hoursB * 60 + minutesB

        // Calculate the difference in minutes
        val minuteDifference = totalMinutesB - totalMinutesA

        // Convert minute difference to hours
        return minuteDifference / 60.0f
    }

    fun isBetweenTime(startTime:String, endTime:String, today:String):Boolean{
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val startDate = sdf.parse(startTime)
            val endDate = sdf.parse(endTime)
            val todayDate = sdf.parse(currentDate)



            if(startDate!= null && endDate!= null && todayDate!= null ){
                return !todayDate.before(startDate) && !todayDate.after(endDate)
            }
            return false
        }catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun getCurrentUserId(): String? {
        val currentUser = auth.currentUser
        return currentUser?.uid
    }

    fun getUsernameFromUserId(userId: String, callback: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(userId)

        database.child("name").get()
            .addOnSuccessListener { snapshot ->
                val username = snapshot.getValue(String::class.java)
                callback(username)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    @SuppressLint("DefaultLocale")
    fun formatLabelHoursSlider(value: Float): String {  // Ví dụ convert 1.5 sẽ trở thành "01:30"
        val hours = value.toInt()
        val minutes = ((value - hours) * 60).toInt()
        return String.format("%02d:%02d", hours, minutes)
    }

    fun getIntFromJobType(jobType: String): Int {
        return when (jobType) {
            "Retail Sales Associate", "Nhân viên bán hàng" -> 1
            "Food and Beverage Server", "Phục vụ" -> 2
            "Administrative Assistant", "Trợ lý hành chính" -> 3
            "Tutor", "Gia sư" -> 4
            "Barista", "Pha chế cà phê" -> 5
            "Cashier", "Thu ngân" -> 6
            "Delivery Driver", "Nhân viên giao hàng" -> 7
            "Receptionist", "Lễ tân" -> 8
            "Other", "Khác" -> 9
            else -> 9
        }
    }

    fun getStringFromJobTypeInt(context: Context, i: Int): String {
        val jobTypesArray = context.resources.getStringArray(R.array.job_types_array)
        return if (i in 1..jobTypesArray.size) {
            jobTypesArray[i - 1]
        } else {
            jobTypesArray.last()  // This will return "Other" as the default case
        }
    }


}