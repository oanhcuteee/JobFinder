package com.example.jobfinder.UI.UsersProfile

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.jobfinder.R
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FragmentHelper.replaceFragment(supportFragmentManager, binding.profileframelayout, SettingsMenuFragment())
        binding.animationView.visibility = View.GONE
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.profileframelayout.id, fragment)
            .addToBackStack(null) // Thêm fragment vào stack để có thể quay lại
            .commit()
    }

}