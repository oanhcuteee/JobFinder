package com.example.jobfinder.UI.JobEmpList

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.ApplicantsModel
import com.example.jobfinder.Datas.Model.CheckInFromBUserModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.google.firebase.database.FirebaseDatabase

class JobEmpListAdapter(private var applicantList: MutableList<ApplicantsModel>,
                        private val context: Context,
                        private val job_id:String
) :
    RecyclerView.Adapter<JobEmpListAdapter.EmpInJobViewHolder>() {

    class EmpInJobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.emp_in_job_username)
        val imgView :ImageView = itemView.findViewById(R.id.emp_in_job_user_avt)
        val checkBtn:Button = itemView.findViewById(R.id.check_in_btn)
        val checkInTime:TextView = itemView.findViewById(R.id.nuser_check_in_time)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpInJobViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_emp_in_job_model, parent, false)

        return EmpInJobViewHolder(itemView)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: EmpInJobViewHolder, position: Int) {

        val currentItem = applicantList[position]
        holder.textViewName.text = currentItem.userName
        holder.checkInTime.visibility= View.GONE

        RetriveImg.retrieveImage(currentItem.userId.toString(), holder.imgView)

        val nUserCheckInDb = FirebaseDatabase.getInstance().getReference("NUserCheckIn").child(job_id)
        val checkInDb = FirebaseDatabase.getInstance().getReference("CheckInFromBUser").child(job_id)
        val today = GetData.getCurrentDateTime()
        val todayTime = GetData.getTimeFromString(today)
        val currentDayString = GetData.getDateFromString(today)
        val currentDay = GetData.formatDateForFirebase(currentDayString)
        // lấy dữ liệu điểm danh của nhân viên
        nUserCheckInDb.child(currentDay).child(currentItem.userId.toString()).get().addOnSuccessListener { dataSnapshot ->
            // kiểm tra xem đã xác nhận rằng nhân viên đã check in
            checkInDb.child(currentDay).child(currentItem.userId.toString()).get().addOnSuccessListener {

                val nUserCheckInTime =
                    dataSnapshot.child("checkInTime").getValue(String::class.java).toString()

                val nUserCheckOutTime =
                    dataSnapshot.child("checkOutTime").getValue(String::class.java).toString()

                val checkStatus = dataSnapshot.child("status").getValue(String::class.java).toString()

                if(checkStatus!= "checked out") {
                    // nếu nhân viên đã điểm danh nhưng buser chưa xác nhận thì sẽ hiện nút xác nhận điểm danh
                    if (dataSnapshot.exists() && !it.exists()) {
                        holder.checkInTime.text =
                            "${context.getText(R.string.check_in_status)} $nUserCheckInTime"
                        holder.checkInTime.visibility = View.VISIBLE
                        holder.checkBtn.setOnClickListener {
                            setConfirmBtn(holder.checkBtn)


                            val checkIn = CheckInFromBUserModel(
                                currentItem.userId.toString(),
                                today,
                                todayTime,
                                "",
                                "confirm check in",
                                "0"
                            )

                            checkInDb.child(currentDay).child(currentItem.userId.toString())
                                .setValue(checkIn)

                            val updateConfirmStatus = hashMapOf<String, Any>(
                                "status" to "comfirmed checked in"
                            )

                            nUserCheckInDb.child(currentDay).child(currentItem.userId.toString())
                                .updateChildren(updateConfirmStatus)
                        }
                    }
                    // nếu đã xác nhận điểm danh thì hiển thị đã xác nhận điêm danh
                    if (dataSnapshot.exists() && it.exists()) {
                        holder.checkInTime.text =
                            "${context.getText(R.string.check_in_status)} $nUserCheckInTime"
                        holder.checkInTime.visibility = View.VISIBLE
                        setConfirmBtn(holder.checkBtn)
                    }
                }else{
                    holder.checkInTime.text =
                        "${context.getText(R.string.check_out_status)} $nUserCheckOutTime"
                    holder.checkInTime.visibility = View.VISIBLE
                    setCheckOutBtn(holder.checkBtn)
                }
                if(!dataSnapshot.exists()){
                    holder.checkBtn.visibility = View.GONE
                }
            }

        }.addOnFailureListener{
            // Handle failure here if necessary
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<ApplicantsModel>) {
        applicantList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = applicantList.size

    private fun setConfirmBtn(checkBtn:Button){
        checkBtn.isClickable = false
        checkBtn.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context,
                R.color.gray
            )
        )
        checkBtn.setText(R.string.confirmed)
        checkBtn.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    private fun setCheckOutBtn(checkBtn:Button){
        checkBtn.isClickable = false
        checkBtn.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context,
                R.color.gray
            )
        )
        checkBtn.setText(R.string.checked_out)
        checkBtn.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

}
