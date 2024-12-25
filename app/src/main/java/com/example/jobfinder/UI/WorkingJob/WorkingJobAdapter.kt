package com.example.jobfinder.UI.WorkingJob

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData

class WorkingJobAdapter(private val context: Context, private var jobList: List<JobModel>) :
    RecyclerView.Adapter<WorkingJobAdapter.PostedJobViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(job: JobModel)
    }

    private var listener: OnItemClickListener? = null

    // Phương thức để thiết lập listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<JobModel>) {
        jobList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostedJobViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_working_job, parent, false)
        return PostedJobViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostedJobViewHolder, position: Int) {
        val job = jobList[position]
        val today = GetData.getCurrentDateTime()
        val todayDate = GetData.getDateFromString(today)
        val dayOfStartDateToToday = GetData.countDaysBetweenDates(job.startTime.toString(), todayDate)
        val dayWork = GetData.countDaysBetweenDates(job.startTime.toString(), job.endTime.toString())

        // Bind data to views
        holder.jobTitleTextView.text = job.jobTitle
        holder.numOfRecruitsTextView.text = job.empAmount
        holder.numOfRecruitedTxtView.text= job.numOfRecruited
        val progress = (dayOfStartDateToToday.toDouble() / dayWork.toDouble()) * 100
        holder.progressBar.setProgress(progress.toInt(), true)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(job)
        }
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    inner class PostedJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitleTextView: TextView = itemView.findViewById(R.id.posted_job_job_title)
        val numOfRecruitsTextView: TextView = itemView.findViewById(R.id.NumOfRecruits)
        val numOfRecruitedTxtView: TextView = itemView.findViewById(R.id.NumOfRecruited)
        val progressBar:ProgressBar = itemView.findViewById(R.id.progress_bar)
    }

}
