package com.example.jobfinder.Utils

import android.os.SystemClock

object PreventDoubleClick {
    // Khoảng cách thời gian click vào cùng 1 button là 1,5s
    private var mLastClickTime: Long = 0
    private val clickInterval: Long = 1500

    fun checkClick(): Boolean {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - mLastClickTime >= clickInterval) {
            mLastClickTime = currentTime
            return true
        }
        return false
    }

}