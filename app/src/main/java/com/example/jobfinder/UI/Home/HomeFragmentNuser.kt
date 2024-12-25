package com.example.jobfinder.UI.Home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jobfinder.UI.AppliedJobs.AppliedJobsActivity
import com.example.jobfinder.UI.CheckIn.Check_In_Activity
import com.example.jobfinder.UI.FindNewJobs.NewJobActivity
import com.example.jobfinder.UI.Report.SupportActivity
import com.example.jobfinder.UI.Statistical.StatisticalActivity
import com.example.jobfinder.UI.Wallet.WalletActivity
import com.example.jobfinder.databinding.FragmentHomeNuserBinding


class HomeFragmentNuser : Fragment() {
    private lateinit var binding: FragmentHomeNuserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeNuserBinding.inflate(inflater, container, false)

        // mở activity wallet
        binding.walletBtn.setOnClickListener{
            startActivity(Intent(requireContext(), WalletActivity::class.java))
        }

        // mở activity tìm job
        binding.JobsearchBtn.setOnClickListener{
            startActivity(Intent(requireContext(), NewJobActivity::class.java))
        }

        binding.JobappliedBtn.setOnClickListener(){
            startActivity(Intent(requireContext(), AppliedJobsActivity::class.java))
        }

        binding.reportsBtn.setOnClickListener(){
            startActivity(Intent(requireContext(), SupportActivity::class.java))
        }

        binding.checkinBtn.setOnClickListener {
            startActivity(Intent(requireContext(), Check_In_Activity::class.java))
        }

        binding.statisticalBtn.setOnClickListener {
            startActivity(Intent(requireContext(), StatisticalActivity::class.java))
        }

        return binding.root
    }


}