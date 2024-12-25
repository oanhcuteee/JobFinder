package com.example.jobfinder.UI.SalaryTracking

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Datas.Model.CheckInFromBUserModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.CheckTime
import com.example.jobfinder.Utils.GetData
import java.text.NumberFormat
import java.util.Currency

class SalaryTrackingAdapter(private val context: Context,
                            private var checkInList: List<CheckInFromBUserModel>,
                            private val appliedJob:AppliedJobModel) :
    RecyclerView.Adapter<SalaryTrackingAdapter.PostedcheckInViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<CheckInFromBUserModel>) {
        checkInList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostedcheckInViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_salary_tracking_model, parent, false)
        return PostedcheckInViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PostedcheckInViewHolder, position: Int) {
        val checkIn = checkInList[position]

        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("VND")

        // Bind data to views
        holder.checkInDate.text = GetData.getDateFromString(checkIn.date.toString())
        holder.checkTime.text = "${checkIn.checkInTime}-${checkIn.checkOutTime}"
        setNote(checkIn.checkInTime.toString(), holder.note)
        holder.workDaySalary.text = format.format(checkIn.salary.toString().toInt())
    }

    override fun getItemCount(): Int {
        return checkInList.size
    }

    inner class PostedcheckInViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkInDate: TextView = itemView.findViewById(R.id.work_day_txt)
        val checkTime: TextView = itemView.findViewById(R.id.check_in_out_time)
        val note:TextView = itemView.findViewById(R.id.check_note)
        val workDaySalary:TextView = itemView.findViewById(R.id.work_day_salary)
    }

    private fun setNote(checkInTime:String,note:TextView){
        if(CheckTime.checkTimeBefore(checkInTime, appliedJob.startHr.toString())){
            note.text = context.getText(R.string.in_time)
        }else{
            val lateMinute = CheckTime.calculateMinuteDiff(appliedJob.startHr.toString(), checkInTime)
            val lateMessage = "${context.getString(R.string.late)} $lateMinute ${context.getString(R.string.minute)}"
            note.text = lateMessage
        }
    }

}

