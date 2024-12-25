package com.example.jobfinder.UI.Notifications
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R

class NotificationDetailDialog(context: Context, private val notificationDetail: NotificationsRowModel?) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.noti_detail_data)

        // Kiểm tra xem có thông báo nào được chọn không
        notificationDetail?.let { notification ->
            // Hiển thị thông tin chi tiết thông báo
//            findViewById<TextView>(R.id.notiDetailUsername).text = notification.from
            findViewById<TextView>(R.id.notiDetail).text = notification.detail

            // Đặt sự kiện cho nút hủy
            findViewById<Button>(R.id.cancel_detail_noti_btn).setOnClickListener {
                dismiss()
            }
        }
    }
}

