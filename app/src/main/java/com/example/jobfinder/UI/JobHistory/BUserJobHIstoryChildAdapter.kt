package com.example.jobfinder.UI.JobHistory

import com.example.jobfinder.Datas.Model.JobHistoryModel
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.R
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.Utils.VerifyField
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class BUserJobHIstoryChildAdapter(private var jobList: MutableList<JobHistoryModel>) :
    RecyclerView.Adapter<BUserJobHIstoryChildAdapter.BUserJobHistoryChildViewHolder>() {

    var isDialogShown = false

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<JobHistoryModel>) {
        jobList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateJob(position: Int, updatedJob: JobHistoryModel) {
        jobList[position] = updatedJob
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BUserJobHistoryChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_buser_job_history_model, parent, false)
        return BUserJobHistoryChildViewHolder(view)
    }

    override fun onBindViewHolder(holder: BUserJobHistoryChildViewHolder, position: Int) {
        val job = jobList[position]
        RetriveImg.retrieveImage(job.nUserId.toString(), holder.nUserAvt)
        holder.nUserNameTxt.text = job.nUserName
        holder.reviewStatusTxt.text = if (job.review=="") {
            holder.itemView.context.getString(R.string.not_review)
        } else {
            job.review
        }
        holder.rating.rating = job.rating?.toFloatOrNull() ?: 0f
        holder.wrapper.setOnClickListener {
            if (!isDialogShown) {
                showOptionsDialog(job, holder.itemView.context, holder.reviewStatusTxt, position, holder.rating)
                isDialogShown = true
            }
        }
    }

    override fun getItemCount(): Int {
        return jobList.size
    }

    inner class BUserJobHistoryChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nUserNameTxt: TextView = itemView.findViewById(R.id.jh_nusername)
        val reviewStatusTxt: TextView = itemView.findViewById(R.id.rv_status)
        val nUserAvt: ImageView = itemView.findViewById(R.id.nuser_ava)
        val rating: RatingBar = itemView.findViewById(R.id.ratingID)
        val wrapper: RelativeLayout = itemView.findViewById(R.id.jH_rv_holder)
    }

    private fun showOptionsDialog(job: JobHistoryModel, context: Context, reviewStatusTxt: TextView, position: Int, ratingBar: RatingBar) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_review_model)

        val rating: RatingBar = dialog.findViewById(R.id.rating_star)
        val reviewDes: TextInputEditText = dialog.findViewById(R.id.jH_description)
        val cancelBtn: Button = dialog.findViewById(R.id.jH_cancel_btn)
        val saveBtn: Button = dialog.findViewById(R.id.jH_send)

//        rating.rating = job.rating.toString().toFloat()
        rating.rating = job.rating?.toFloatOrNull() ?: 0.0f
        reviewDes.setText(job.review)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
            isDialogShown = false
        }

        saveBtn.setOnClickListener {
            val reviewDesTxt = reviewDes.text.toString()
            val ratingNum = rating.rating

            val isValidReviewDes = VerifyField.isEmpty(reviewDesTxt.trim())
            reviewDes.error = if (isValidReviewDes) null else context.getString(R.string.no_des)
            if (isValidReviewDes) {
                val database = FirebaseDatabase.getInstance().getReference("NUserJobHistory")
                val bUserDb = FirebaseDatabase.getInstance().getReference("BUserJobHistory")

                val updateRv = hashMapOf<String, Any>(
                    "rating" to ratingNum.toString(),
                    "review" to reviewDesTxt
                )

                bUserDb.child(job.BUserId.toString()).child(job.jobId.toString()).child(job.nUserId.toString()).updateChildren(updateRv)
                database.child(job.nUserId.toString()).child(job.jobId.toString()).updateChildren(updateRv)

                job.review = reviewDesTxt
                job.rating = ratingNum.toString()

                updateJob(position, job)

                reviewStatusTxt.text = reviewDesTxt
                ratingBar.rating = ratingNum

                dialog.dismiss()
                isDialogShown = false
            } else {
                reviewDes.requestFocus()
            }
        }
        isDialogShown = false
        dialog.show()
    }
}
