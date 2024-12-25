package com.example.jobfinder.UI.Admin.Home

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.jobfinder.R
import com.example.jobfinder.UI.UsersProfile.ChangePasswordFragment
import com.example.jobfinder.Utils.FragmentHelper.replaceFragment
import com.example.jobfinder.databinding.ActivityAdminChangePassBinding
import com.example.jobfinder.databinding.ActivityAdminHomeBinding

class AdminChangePassActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminChangePassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminChangePassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragment(supportFragmentManager, binding.AdminChangePassFramelayout, ChangePasswordFragment())

    }
}