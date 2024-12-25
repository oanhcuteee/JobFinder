package com.example.jobfinder.UI.WorkingJob

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.R

class NUserWorkingJobAdapter(private val context: Context, private var jobList: List<AppliedJobModel>) :
    RecyclerView.Adapter<NUserWorkingJobAdapter.PostedJobViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(job: AppliedJobModel)
    }

    private var listener: OnItemClickListener? = null

    // Phương thức để thiết lập listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<AppliedJobModel>) {
        jobList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostedJobViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_nuser_working_job_model, parent, false)
        return PostedJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostedJobViewHolder, position: Int) {
        val job = jobList[position]

        // Bind data to views
        holder.jobTitleTextView.text = job.jobTitle

        holder.itemView.setOnClickListener {
            listener?.onItemClick(job)
        }
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    inner class PostedJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitleTextView: TextView = itemView.findViewById(R.id.posted_job_job_title)
    }

}
