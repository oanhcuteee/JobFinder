package com.example.jobfinder.UI.UsersProfile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jobfinder.R
import com.example.jobfinder.UI.ForgotPassword.ForgotPassFragment
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.databinding.ActivityProfileForgotPasswordBinding

class ProfileForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FragmentHelper.replaceFragment(supportFragmentManager, binding.profileframelayout, ForgotPassFragment())
        binding.animationView.visibility = View.GONE

        binding.backbtn.setOnClickListener(){
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }
}