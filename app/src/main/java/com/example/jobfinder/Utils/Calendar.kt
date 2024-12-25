package com.example.jobfinder.Utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import com.google.android.material.textfield.TextInputEditText
import android.content.Context
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout


object Calendar {
    fun setupDatePicker(context: Context, editText: TextInputEditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            },
            year,
            month,
            day
        )

        // Cộng thêm 2 ngày vào ngày hiện tại để đặt minDate
        calendar.add(java.util.Calendar.DAY_OF_MONTH, 2)
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        // Set the minimum date to the current date
        //datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    fun showTimePickerDialog(context: Context, editText: EditText) {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                // Update EditText with selected time
                editText.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

}