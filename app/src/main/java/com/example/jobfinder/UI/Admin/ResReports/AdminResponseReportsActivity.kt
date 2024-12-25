package com.example.jobfinder.UI.Admin.ResReports

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.jobfinder.R
import com.example.jobfinder.databinding.ActivityAdminResponseReportsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class AdminResponseReportsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdminResponseReportsBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminResponseReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.backButton.setOnClickListener {
            sendResultAndFinish()
        }

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        setupViewPager()
        removeTabMargins(tabLayout)

        binding.RRSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                setupViewPager()
                removeTabMargins(tabLayout)
                binding.RRSwipe.isRefreshing = false
            }, 1000)
        }

    }

    private fun sendResultAndFinish() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        sendResultAndFinish()
    }


    @SuppressLint("InflateParams")
    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = LayoutInflater.from(this@AdminResponseReportsActivity).inflate(R.layout.cus_tab_layout_jobhistory, null)
            val tabTextView = customView.findViewById<TextView>(R.id.tabTextView)
            when (position) {
                0 -> tabTextView.text = getString(R.string.technic_title)
                1 -> tabTextView.text = getString(R.string.feedback_title)
                2-> tabTextView.text = getString(R.string.report_title)
            }
            tab.customView = customView
        }.attach()
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TechnicalReportFragment(binding.animationView)
                1 -> FeedbackFragment(binding.animationView)
                2 -> ReportFragment(binding.animationView)
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }
    }

    private fun removeTabMargins(tabLayout: TabLayout) {
        val tabStrip = tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tabView = tabStrip.getChildAt(i)
            val layoutParams = tabView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, 0, 0, 0)
            tabView.layoutParams = layoutParams
            tabView.requestLayout()
        }
    }
}