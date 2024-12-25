package com.example.jobfinder.UI.JobHistory

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.JobHistoryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg

class NUserJobHistoryAdapter(private val context: Context, private var jobList: List<JobHistoryModel>) :
    RecyclerView.Adapter<NUserJobHistoryAdapter.NUserJobHistoryViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<JobHistoryModel>) {
        jobList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NUserJobHistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_job_history_model, parent, false)
        return NUserJobHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: NUserJobHistoryViewHolder, position: Int) {
        val job = jobList[position]

        // Bind data to views
        RetriveImg.retrieveImage(job.BUserId.toString(), holder.bUserAvt)
        holder.jobTitleTextView.text = job.jobTitle
        holder.bUserNameTextView.text= job.bUserName
        holder.reviewDetailTextView.text = if(job.review.toString() == "") context.getString(R.string.no_review) else job.review
        holder.reviewDateTxtView.text = GetData.getDateFromString(job.endDate.toString())

        holder.rating.rating = job.rating.toString().toFloat()

    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    inner class NUserJobHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitleTextView: TextView = itemView.findViewById(R.id.jh_jobTitle)
        val bUserNameTextView: TextView = itemView.findViewById(R.id.jh_busername)
        val rating: RatingBar = itemView.findViewById(R.id.jh_ratingID_item)
        val reviewDetailTextView: TextView = itemView.findViewById(R.id.jh_desc)
        val reviewDateTxtView: TextView = itemView.findViewById(R.id.jh_endDate)
        val bUserAvt:ImageView= itemView.findViewById(R.id.user_ava)
    }

}