package com.example.jobfinder.UI.Admin.ResReports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.jobfinder.Datas.Model.SupportUser
import com.example.jobfinder.R
import com.example.jobfinder.databinding.FragmentReportBinding

class ReportFragment (private val animationView: LottieAnimationView) : Fragment() {
    lateinit var binding:FragmentReportBinding
    private val viewModel:AdminResReportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AdminResReportAdapter(mutableListOf(),viewModel)
        binding.recyclerReportList.adapter = adapter
        binding.recyclerReportList.layoutManager = LinearLayoutManager(requireContext())

        viewModel.reportList.observe(viewLifecycleOwner){ reportList->
            adapter.updateData(reportList)
            checkEmptyAdapter(reportList)
        }

        viewModel.fetchReport()

    }

    private fun checkEmptyAdapter(list: MutableList<SupportUser>) {
        if (list.isEmpty()) {
            binding.noReport.visibility = View.VISIBLE
            animationView.visibility = View.GONE
        } else {
            binding.noReport.visibility = View.GONE
            animationView.visibility = View.GONE
        }
    }
}