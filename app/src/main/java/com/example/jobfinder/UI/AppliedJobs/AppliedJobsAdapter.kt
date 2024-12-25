package com.example.jobfinder.UI.AppliedJobs

import android.icu.text.NumberFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData.getDateFromString
import com.example.jobfinder.Utils.RetriveImg
import java.util.Currency
import java.util.Locale

class AppliedJobsAdapter(
    private var appliedList : List<AppliedJobModel>,
    private var noDataImage: LinearLayout,
) : RecyclerView.Adapter<AppliedJobsAdapter.MyViewHolder>() {

    lateinit var mListener: AppliedJobsAdapter.onItemClickListener

    interface onItemClickListener {
        fun onItemClicked(AppliedJob: AppliedJobModel) {}
    }
    fun setOnItemClickListener(listener: AppliedJobsAdapter.onItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.applied_job_item, parent, false)
        return MyViewHolder(view, mListener)
    }

    override fun getItemCount(): Int {
        return appliedList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Định dạng giá trị salary với dấu phẩy VNĐ
        val format = java.text.NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("VND")

        println("${appliedList[position].buserId}")
        RetriveImg.retrieveImage(appliedList[position].buserId.toString(), holder.avatar)
        holder.jobTitle.text = appliedList[position].jobTitle?.uppercase(Locale.getDefault())
        holder.appliedDate.text = getDateFromString(appliedList[position].appliedDate.toString())
        holder.startHr.text = appliedList[position].startHr
        holder.endHr.text = appliedList[position].endHr
        holder.salary.text = format.format(appliedList[position].salary?.toDouble())
    }

    inner class MyViewHolder(view: View, listener: AppliedJobsAdapter.onItemClickListener) : RecyclerView.ViewHolder(view){
        val avatar : ImageView
        val jobTitle : TextView
        val appliedDate: TextView
        val startHr: TextView
        val endHr: TextView
        val salary: TextView

        init{
            avatar = view.findViewById(R.id.user_ava)
            jobTitle = view.findViewById(R.id.JobTitle)
            appliedDate = view.findViewById(R.id.appliedDate)
            startHr = view.findViewById(R.id.timeStart)
            endHr = view.findViewById(R.id.timeEnd)
            salary = view.findViewById(R.id.salary)

            view.setOnClickListener{
                val position = bindingAdapterPosition // Lấy vị trí của item trong danh sách
                if (position != RecyclerView.NO_POSITION) {
                    val AppliedJob = appliedList[position] // Lấy AppliedJobModel tương ứng với vị trí
                    listener.onItemClicked(AppliedJob) // Gọi phương thức onItemClicked với JobModel
                }
            }

        }
    }

    fun updateData(newList: List<AppliedJobModel>) {
        appliedList = newList
        notifyDataSetChanged()
    }

    fun showNoDataFoundImg() {
        noDataImage.visibility = View.VISIBLE
    }

    fun hideNoDataFoundImg() {
        noDataImage.visibility = View.GONE
    }
}