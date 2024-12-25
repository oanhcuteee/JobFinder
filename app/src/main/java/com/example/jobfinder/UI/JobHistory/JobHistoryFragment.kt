package com.example.jobfinder.UI.JobHistory

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.jobfinder.Datas.Model.JobHistoryModel
import com.example.jobfinder.databinding.FragmentJobHistoryBinding

class JobHistoryFragment(private val animationView: LottieAnimationView) : Fragment() {

    private val viewModel: JobHistoryViewModel by viewModels()
    private lateinit var binding: FragmentJobHistoryBinding
    private lateinit var adapter:NUserJobHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentJobHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animationView.visibility= View.VISIBLE

        // Tạo adapter và gán vào RecyclerView
        adapter = NUserJobHistoryAdapter(binding.root.context, mutableListOf())
        binding.recyclerWorkingJob.adapter = adapter
        binding.recyclerWorkingJob.layoutManager = LinearLayoutManager(requireContext())

        viewModel.JobHistoryList.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        viewModel.fetchNUserJobHistory()

    }

    private fun checkEmptyAdapter(list: MutableList<JobHistoryModel>) {
        if (list.isEmpty()) {
            binding.noJob.visibility = View.VISIBLE
            animationView.visibility = View.GONE
        } else {
            binding.noJob.visibility = View.GONE
            animationView.visibility = View.GONE
        }
    }
}