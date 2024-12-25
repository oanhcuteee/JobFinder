package com.example.jobfinder.UI.UsersProfile

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.databinding.ActivityUserDetailBinding

class UserDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailBinding
    private var backCheck = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            if(backCheck){
                FragmentHelper.replaceFragment(
                    supportFragmentManager,
                    binding.profileframelayout,
                    UserProfileMenuFragment()
                )}else {
                val resultIntent = Intent()
                setResult(Activity.RESULT_OK, resultIntent)
                finish()

            }
        }

    }

    override fun onStart() {
        super.onStart()
        // Thêm fragment mặc định khi hoạt động bắt đầu
        FragmentHelper.replaceFragment(
            supportFragmentManager,
            binding.profileframelayout,
            UserProfileMenuFragment()
        )
    }



}
