package com.example.jobfinder.UI.Notifications

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R
import com.example.jobfinder.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Khởi tạo viewmodel nếu fragment đã dc add vào activity
        if (isAdded) {
            viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)
        }

        binding.deleteAll.setOnClickListener{
            viewModel.deleteAllNoti()
            viewModel.fetchNotificationsFromFirebase()
        }

        binding.notiSwipe.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.fetchNotificationsFromFirebase()
                binding.notiSwipe.isRefreshing = false
            }, 1000)
        }

        // Quan sát dữ liệu trong ViewModel và cập nhật giao diện khi có thay đổi
        viewModel.notificationList.observe(viewLifecycleOwner) { list ->
            updateRecyclerView(list)
        }

        // Kiểm tra xem dữ liệu đã được lưu trong ViewModel chưa
        if (viewModel.notificationList.value == null) {
            // Nếu chưa có dữ liệu, thực hiện fetch từ Firebase
            viewModel.fetchNotificationsFromFirebase()
        } else {
            // Nếu đã có dữ liệu, cập nhật RecyclerView trực tiếp
            updateRecyclerView(viewModel.notificationList.value!!)
        }
    }


    private fun updateRecyclerView(notificationList: List<NotificationsRowModel>) {
        val convertToMutableList = notificationList.toMutableList()
        // Cập nhật RecyclerView với danh sách thông báo mới
        val adapter = NotificationsAdapter(convertToMutableList, requireContext(), binding.noNoti)
        binding.recyclerNotifications.adapter = adapter
        binding.recyclerNotifications.layoutManager = LinearLayoutManager(requireContext())

        // Xác định nếu người dùng nhấn vào nút "Xóa"
        adapter.setOnItemClickListener(object : NotificationsAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View,
                position: Int,
                item: NotificationsRowModel
            ) {
                if (view.id == R.id.txtDelete) {
                    adapter.removeItem(position)
                    viewModel.deleteSingleNoti(item.notiId.toString())
                }
            }
        })
        binding.animationView.visibility = View.GONE
    }

}