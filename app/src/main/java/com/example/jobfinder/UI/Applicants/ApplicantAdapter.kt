package com.example.jobfinder.UI.Applicants

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Datas.Model.SalaryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.google.firebase.database.FirebaseDatabase

class ApplicantAdapter(private var applicantList: MutableList<ApplicantsModel>,
                       private val job:JobModel,
                       private val context: android.content.Context,
                       private val viewModel: ApplicantViewModel) :
    RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(applicant: ApplicantsModel)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class ApplicantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.applicant_username)
        val textViewDescription: TextView = itemView.findViewById(R.id.applicant_des)
        val imgView :ImageView = itemView.findViewById(R.id.applicant_user_avt)
        val approveBtn :Button = itemView.findViewById(R.id.approve_btn)
        val rejectBtn : Button= itemView.findViewById(R.id.reject_btn)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicantViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_applicant_model, parent, false)

        return ApplicantViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ApplicantViewHolder, position: Int) {
        val currentItem = applicantList[position]
        holder.textViewName.text = currentItem.userName
        if(currentItem.applicantDes == ""){
            holder.textViewDescription.text = holder.itemView.context.getString(R.string.no_job_des2)
        }else {
            holder.textViewDescription.text = currentItem.applicantDes
        }
        val position = holder.adapterPosition

        val notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(currentItem.userId.toString())
        val appliedJobRef = FirebaseDatabase.getInstance().getReference("AppliedJob").child(currentItem.userId.toString()).child(job.jobId.toString())
        val curTime = GetData.getCurrentDateTime()

        RetriveImg.retrieveImage(currentItem.userId.toString(), holder.imgView)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem)
        }

        holder.approveBtn.setOnClickListener {

            if (position != RecyclerView.NO_POSITION) {
                // add recruitedEmp
                val jobRef = FirebaseDatabase.getInstance().getReference("Job").child(job.BUserId.toString()).child(job.jobId.toString())
                jobRef.get().addOnSuccessListener { data ->
                    val recruitedAmount = data.child("numOfRecruited").getValue(String::class.java).toString()
                    val empAmount = data.child("empAmount").getValue(String::class.java).toString()
                    if(recruitedAmount.toInt() < empAmount.toInt()){

                        viewModel.deleteApplicant(job.jobId.toString() ,currentItem.userId.toString())
                        applicantList.removeAt(position)
                        notifyItemRemoved(position)

                        val newRecuitedAmount = (recruitedAmount.toInt() + 1).toString()

                        jobRef.child("numOfRecruited").setValue(newRecuitedAmount)

                        appliedJobRef.removeValue()

                        val EmpInJob = ApplicantsModel(currentItem.userId, currentItem.applicantDes, curTime, currentItem.userName)

                        FirebaseDatabase.getInstance().getReference("EmpInJob").child(job.jobId.toString()).child(currentItem.userId.toString()).setValue(EmpInJob)

                        val approvedJob = AppliedJobModel(job.BUserId.toString(), job.jobId.toString(), curTime, job.jobTitle.toString(), job.startHr.toString(), job.endHr.toString(), job.salaryPerEmp.toString(), job.postDate.toString(), job.startTime.toString(), job.endTime.toString())

                        FirebaseDatabase.getInstance().getReference("ApprovedJob").child(currentItem.userId.toString()).child(job.jobId.toString()).setValue(approvedJob)

                        //salary
                        val totalWorkDay = GetData.countDaysBetweenDates(job.startTime.toString(), job.endTime.toString())
                        val salary = SalaryModel(totalWorkDay, 0, 0f)

                        FirebaseDatabase.getInstance().getReference("Salary")
                            .child(job.jobId.toString()).child(currentItem.userId.toString())
                            .setValue(salary)
                        // notification
                        val notiId = notiRef.push().key.toString()
                        val notification = NotificationsRowModel(notiId, job.BUserName.toString(),
                            getString(context,R.string.approve_from) + " ${job.jobTitle.toString()}"
                            ,curTime)
                        notiRef.child(notiId).setValue(notification)

                        Toast.makeText(
                            context,
                            getString( context,R.string.approve_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            context,
                            getString( context,R.string.enough_Emp),
                            Toast.LENGTH_SHORT
                        ).show()

                        //khi lấy đủ nhân viên tự xóa hết trong appliedJob của tất cả những ứng viên
                        FirebaseDatabase.getInstance().getReference("AppliedJob").get().addOnSuccessListener {
                            for( uid in it.children){

                                viewModel.deleteApplicant(job.jobId.toString() ,uid.key.toString())
                                applicantList.clear()
                                FirebaseDatabase.getInstance().getReference("AppliedJob").child(uid.key.toString()).child(job.jobId.toString()).removeValue()

                                val notiId = FirebaseDatabase.getInstance().getReference("Notifications").child(uid.key.toString()).push().key.toString()
                                val notification = NotificationsRowModel(notiId, job.BUserName.toString(),
                                    getString(context,R.string.reject_from) + " ${job.jobTitle.toString()}"
                                    ,curTime)
                                FirebaseDatabase.getInstance().getReference("Notifications").child(uid.key.toString()).child(notiId).setValue(notification)

                            }
                        }
                        //xóa hết danh sách ứng viên khi đã full
                        FirebaseDatabase.getInstance().getReference("Applicant").child(job.jobId.toString()).removeValue()
                        notifyDataSetChanged()

                    }

                }

            }
        }

        holder.rejectBtn.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                viewModel.deleteApplicant(job.jobId.toString() ,currentItem.userId.toString())
                applicantList.removeAt(position)
                notifyItemRemoved(position)

                // notification
                val notiId = notiRef.push().key.toString()
                val notification = NotificationsRowModel(notiId, job.BUserName.toString(),
                    getString(context,R.string.reject_from) + " ${job.jobTitle.toString()}"
                    ,curTime)
                notiRef.child(notiId).setValue(notification)

                appliedJobRef.removeValue()

                Toast.makeText(
                    context,
                    getString( context,R.string.reject_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<ApplicantsModel>) {
        applicantList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = applicantList.size

}
