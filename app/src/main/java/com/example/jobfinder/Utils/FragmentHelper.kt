package com.example.jobfinder.Utils

import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewbinding.ViewBinding
import com.example.jobfinder.R

object FragmentHelper {
    fun replaceFragment(fragmentManager: FragmentManager, frame: FrameLayout, fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(frame.id, fragment)
        fragmentTransaction.commit()
    }

    fun replaceFragmentCallBack(fragmentManager: FragmentManager, frame: FrameLayout, fragment: Fragment, onFragmentAddedListener: OnFragmentAddedListener?) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(frame.id, fragment)
        fragmentTransaction.commit()

        // Kiểm tra và gọi callback nếu nó được cung cấp
        onFragmentAddedListener?.invoke()
    }
}

// Khai báo một typealias để định nghĩa một hàm callback khi fragment được thêm vào thành công (phần wallet)
typealias OnFragmentAddedListener = () -> Unit