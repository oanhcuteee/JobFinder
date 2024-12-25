package com.example.jobfinder.UI.AboutUs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.R
import com.example.jobfinder.databinding.ActivityAboutusBinding
import com.google.android.material.tabs.TabLayoutMediator

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutusBinding
    private lateinit var adapter: AboutUsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // nút back về
        binding.backbtn.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


        // adapter
        val pages = listOf(
            AboutUsAdapter.AboutPage(R.drawable.aboutus_aboutapp, getString(R.string.about_title_app), getString(R.string.about_desc_app)),
            AboutUsAdapter.AboutPage(R.drawable.aboutus_aboutus, getString(R.string.about_title_us), getString(R.string.about_desc_us)),
            AboutUsAdapter.AboutPage(R.drawable.aboutus_goals, getString(R.string.about_title_goals), getString(R.string.about_desc_goals)),
            AboutUsAdapter.AboutPage(R.drawable.aboutus_feaandben, getString(R.string.about_title_feaAndBen), getString(R.string.about_desc_feaAndBen))
        )
        adapter = AboutUsAdapter(pages)
        binding.viewPager.adapter = adapter

        // chấm tròn ở dưới
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            // Không cần đặt tiêu đề cho các tab
            tab.customView = LayoutInflater.from(this).inflate(R.layout.custom_tab_layout_aboutus, null)
        }.attach()


    }
}