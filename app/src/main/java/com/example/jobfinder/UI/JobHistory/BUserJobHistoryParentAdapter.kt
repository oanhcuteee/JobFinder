package com.example.jobfinder.UI.JobHistory

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.JobHistoryParentModel
import com.example.jobfinder.R

class BUserJobHistoryParentAdapter(private var jobList: List<JobHistoryParentModel>) :
    RecyclerView.Adapter<BUserJobHistoryParentAdapter.BUserJobHistoryParentViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<JobHistoryParentModel>) {
        jobList = newList
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BUserJobHistoryParentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_buser_job_history_job_id_model, parent, false)
        return BUserJobHistoryParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: BUserJobHistoryParentViewHolder, position: Int) {
        val job = jobList[position]


        holder.jobTitleTextView.text = job.jobTitle
        holder.jobTypeTextView.text = job.jobType
        val adapter = BUserJobHIstoryChildAdapter(job.childernList)
        holder.childRecyclerView.adapter= adapter
        holder.childRecyclerView.layoutManager= LinearLayoutManager(holder.itemView.context)

        // mở ra adapter con
        val isExpanded = job.isExpanded

        holder.childRecyclerView.visibility = if(isExpanded) View.VISIBLE else View.GONE

        holder.jobHolder.setOnClickListener {

            isAnyItemExpanded(position)
            job.isExpanded= !job.isExpanded
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    inner class BUserJobHistoryParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jobTitleTextView: TextView = itemView.findViewById(R.id.jH_job_title)
        val jobTypeTextView: TextView = itemView.findViewById(R.id.jH_job_type)
        val childRecyclerView :RecyclerView= itemView.findViewById(R.id.buser_job_history_recycler)
        val jobHolder:ConstraintLayout = itemView.findViewById(R.id.job_text_holder)
    }

    private fun isAnyItemExpanded(position:Int){
        val temp = jobList.indexOfFirst {
            it.isExpanded
        }

        if(temp >= 0 && temp != position){
            jobList[temp].isExpanded= false
            notifyItemChanged(temp)
        }
    }
}