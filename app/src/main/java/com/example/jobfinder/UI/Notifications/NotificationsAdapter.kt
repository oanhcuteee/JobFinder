package com.example.jobfinder.UI.Notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.RowNotificationsBinding

class NotificationsAdapter(
  var list: MutableList<NotificationsRowModel>,
  private val context: Context,
  private val noNotiLayout: ConstraintLayout
) : RecyclerView.Adapter<NotificationsAdapter.RowNotifications>() {
  private var clickListener: OnItemClickListener? = null
  private var isDialogOpened = false

  init {
    checkEmptyAdapter()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowNotifications {
    val binding = RowNotificationsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return RowNotifications(binding)
  }

  override fun onBindViewHolder(holder: RowNotifications, position: Int) {
    val notificationsRowModel = list[position]
    holder.bind(notificationsRowModel)

    holder.itemView.setOnClickListener {
      if (!isDialogOpened) {
        selectedNotification = notificationsRowModel
        val dialog = NotificationDetailDialog(context, selectedNotification)
        dialog.setOnDismissListener {
          isDialogOpened = false
        }
        dialog.show()
        isDialogOpened = true
      }
    }
  }

  private var selectedNotification: NotificationsRowModel? = null

  override fun getItemCount(): Int = list.size

  fun removeItem(position: Int) {
    if (position in 0 until list.size) {
      list.removeAt(position)
      notifyItemRemoved(position)
      notifyItemRangeChanged(position, list.size - position)
      checkEmptyAdapter()
    }
  }

  private fun checkEmptyAdapter() {
    if (list.isEmpty()) {
      noNotiLayout.visibility = View.VISIBLE
    } else {
      noNotiLayout.visibility = View.GONE
    }
  }

  fun setOnItemClickListener(clickListener: OnItemClickListener) {
    this.clickListener = clickListener
  }

  interface OnItemClickListener {
    fun onItemClick(
      view: View,
      position: Int,
      item: NotificationsRowModel
    )
  }

  inner class RowNotifications(
    private val binding: RowNotificationsBinding
  ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    init {
      binding.txtDelete.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
      if (view == binding.txtDelete) {
        val position = adapterPosition
        if (position != RecyclerView.NO_POSITION) {
          clickListener?.onItemClick(view, position, list[position])
        }
      }
    }

    fun bind(item: NotificationsRowModel) {
      binding.apply {
//        txtApplicationsen.text = item.from
        txtApplicationsfo.text = item.detail
        txtDate.text = GetData.getDateFromString(item.date.toString()).toString()
      }
    }
  }
}
