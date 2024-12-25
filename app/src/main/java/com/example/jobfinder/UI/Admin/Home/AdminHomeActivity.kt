package com.example.jobfinder.UI.Admin.Home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.R
import com.example.jobfinder.UI.Admin.ResReports.AdminResponseReportsActivity
import com.example.jobfinder.UI.Admin.Statistical.AdminStatisticalActivity
import com.example.jobfinder.UI.Admin.UserManagement.AdminUserManagActivity
import com.example.jobfinder.UI.SplashScreen.SelectRoleActivity
import com.example.jobfinder.databinding.ActivityAdminHomeBinding
import com.google.firebase.auth.FirebaseAuth

class AdminHomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminHomeBinding
    private lateinit var auth: FirebaseAuth
    private var backPressedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // trả về result về login admin để đóng activity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)


        // Quản lý tài khoản người dùng
        binding.accManagerBtn.setOnClickListener {
            startActivity(Intent(this, AdminUserManagActivity::class.java))
        }


        // Thống kê
        binding.statisticalBtn.setOnClickListener {
            startActivity(Intent(this, AdminStatisticalActivity::class.java))
        }


        // Phản hồi báo cáo
        binding.ResponseReportsBtn.setOnClickListener {
            startActivity(Intent(this, AdminResponseReportsActivity::class.java))
        }


        // Đăng xuất
        binding.AdminLogoutBtn.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, SelectRoleActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, R.string.profile_logout_toast, Toast.LENGTH_SHORT).show()
            finish()
        }


        binding.AdminChangePassBtn.setOnClickListener {
            startActivity(Intent(this, AdminChangePassActivity::class.java))
        }

    }



    override fun onResume() {
        super.onResume()
        backPressedCount = 0 // Reset lại backPressedCount khi activity resume
    }

    // Bấm 1 lần để hỏi, lần thứ 2 sẽ thoát ứng dụng
    override fun onBackPressed() {
        if (backPressedCount >= 1) {
            setResult(Activity.RESULT_CANCELED)
            super.onBackPressed() // đóng activity
            finish()
        } else {
            backPressedCount++
            Toast.makeText(this, getString(R.string.backpress_ask), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                backPressedCount = 0
            }, 2000) // Reset backPressedCount sau 2 giây
        }
    }
}