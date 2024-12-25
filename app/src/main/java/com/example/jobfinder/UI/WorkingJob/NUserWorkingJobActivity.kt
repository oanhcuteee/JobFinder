package com.example.jobfinder.UI.WorkingJob

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
import com.example.jobfinder.UI.JobHistory.JobHistoryFragment
import com.example.jobfinder.databinding.ActivityNuserWorkingJobBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class NUserWorkingJobActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNuserWorkingJobBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuserWorkingJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            sendResultAndFinish()
        }

        binding.nuserJobManagementSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                setupViewPager()
                removeTabMargins(tabLayout)
                binding.nuserJobManagementSwipe.isRefreshing = false
            }, 1000)
        }

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        setupViewPager()
        removeTabMargins(tabLayout)

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


    private fun setupViewPager() {
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val customView = LayoutInflater.from(this@NUserWorkingJobActivity).inflate(R.layout.cus_tab_layout_jobhistory, null)
            val tabTextView = customView.findViewById<TextView>(R.id.tabTextView)
            when (position) {
                0 -> tabTextView.text = getString(R.string.working_job_short_title)
                1 -> tabTextView.text = getString(R.string.job_workingReview_title)
            }
            tab.customView = customView
        }.attach()
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> NUserWorkingJobFragment(binding.animationView)
                1 -> JobHistoryFragment(binding.animationView)
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