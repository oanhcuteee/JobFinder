package com.example.jobfinder.UI.JobsManagement

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jobfinder.databinding.ActivityJobsmanagementBinding

class JobsManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityJobsmanagementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJobsmanagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        // back bằng nút trên màn hình
        binding.backButton.setOnClickListener{
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    // back bằng nút hoặc vuốt trên thiết bị
    override fun onBackPressed() {
        super.onBackPressed()
        // Khởi tạo Intent để quay lại HomeActivity
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}