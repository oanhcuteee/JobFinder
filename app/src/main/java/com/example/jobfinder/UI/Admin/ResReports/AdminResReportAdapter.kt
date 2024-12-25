package com.example.jobfinder.UI.Admin.ResReports

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Datas.Model.SupportUser
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase

class AdminResReportAdapter(private var reportList: MutableList<SupportUser>, private val viewModel: AdminResReportViewModel) :
    RecyclerView.Adapter<AdminResReportAdapter.ReportViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<SupportUser>) {
        reportList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_report_res_model, parent, false)
        return ReportViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reportList[position]

        // Bind data to views
        holder.reportTitleTextView.text = report.status
        if(report.description == ""){
            holder.reportDesTextView.text = holder.itemView.context.getString(R.string.no_job_des2)
        }else {
            holder.reportDesTextView.text = report.description
        }

        FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(report.userId.toString()).get().addOnSuccessListener {
            if(it.exists()){
                val email = it.child("email").getValue(String::class.java)
                holder.email.text = email
            }
        }
        val position = holder.adapterPosition

        holder.reportCloseTextView.setOnClickListener {
            val date = GetData.getCurrentDateTime()

            FirebaseDatabase.getInstance().getReference("AdminRef").child("Report")
                .child(report.supportId.toString()).removeValue()

            val notiId = FirebaseDatabase.getInstance().getReference("Notifications")
                .child(report.userId.toString()).push().key.toString()
            val notificationsRowModel = NotificationsRowModel(
                notiId, "Admin",
                holder.itemView.context.getString(R.string.report_response1),
                date
            )
            FirebaseDatabase.getInstance().getReference("Notifications")
                .child(report.userId.toString()).child(notiId).setValue(notificationsRowModel)

            viewModel.deleteReport(report.supportId.toString())
            reportList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reportTitleTextView: TextView = itemView.findViewById(R.id.report_res_report_title)
        val reportDesTextView: TextView = itemView.findViewById(R.id.report_des)
        val reportCloseTextView: TextView = itemView.findViewById(R.id.rr_delete_txt)
        val email: TextView= itemView.findViewById(R.id.reporter_email)
    }

//    private fun checkEmptyAdapter() {
//        if (reportList.isEmpty()) {
//            noData.visibility = View.VISIBLE
//        } else {
//            noData.visibility = View.GONE
//        }
//    }
}

