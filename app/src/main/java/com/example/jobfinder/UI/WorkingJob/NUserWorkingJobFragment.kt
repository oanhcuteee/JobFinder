package com.example.jobfinder.UI.WorkingJob

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.UI.CheckIn.CheckInViewModel
import com.example.jobfinder.UI.SalaryTracking.SalaryTrackingActivity
import com.example.jobfinder.databinding.FragmentNUserWorkingJobBinding

class NUserWorkingJobFragment(private val animationView: LottieAnimationView) : Fragment() {
    private lateinit var binding: FragmentNUserWorkingJobBinding
    private val viewModel: CheckInViewModel by viewModels()
    private lateinit var adapter: NUserWorkingJobAdapter
    private val REQUEST_CODE = 1002
    private var isActivityOpened = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNUserWorkingJobBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animationView.visibility= View.VISIBLE

        // Tạo adapter và gán vào RecyclerView
        adapter = NUserWorkingJobAdapter(binding.root.context, mutableListOf())
        binding.recyclerWorkingJob.adapter = adapter
        binding.recyclerWorkingJob.layoutManager = LinearLayoutManager(requireContext())

        viewModel.ApprovedJobList.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateData(updatedList)
            checkEmptyAdapter(updatedList)
        }

        adapter.setOnItemClickListener(object : NUserWorkingJobAdapter.OnItemClickListener {
            override fun onItemClick(job: AppliedJobModel) {
                if (!isActivityOpened) {
                    // Mở activity chỉ khi activity chưa được mở
                    val intent = Intent(requireContext(), SalaryTrackingActivity::class.java)
                    intent.putExtra("approved_job", job)
                    startActivityForResult(intent, REQUEST_CODE)
                    // Đặt biến kiểm tra là đã mở
                    isActivityOpened = true
                }
            }
        })

        viewModel.fetchApprovedJob()

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            isActivityOpened = false
            viewModel.fetchApprovedJob()
        }
    }

    private fun checkEmptyAdapter(list: MutableList<AppliedJobModel>) {
        if (list.isEmpty()) {
            binding.noJob.visibility = View.VISIBLE
            animationView.visibility = View.GONE
        } else {
            binding.noJob.visibility = View.GONE
            animationView.visibility = View.GONE
        }
    }
}