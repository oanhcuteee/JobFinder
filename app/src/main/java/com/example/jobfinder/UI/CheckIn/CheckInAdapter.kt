package com.example.jobfinder.UI.CheckIn

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.Datas.Model.CheckInFromBUserModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.Statistical.IncomeViewModel
import com.example.jobfinder.UI.Statistical.WorkHoursViewModel
import com.example.jobfinder.Utils.CheckTime
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase
import kotlin.math.roundToInt

class CheckInAdapter(private var approvedJobList: MutableList<AppliedJobModel>,
                        private val context: Context,
                        private val viewModel: IncomeViewModel,
                        private val workHourViewModel: WorkHoursViewModel
) :
    RecyclerView.Adapter<CheckInAdapter.CheckInViewHolder>() {
    private val LATEST_TIME = 60
    private val LATE_MINUS_PERCENT = 85f/100

    class CheckInViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewJobTitle: TextView = itemView.findViewById(R.id.check_in_job_title)
        val checkInTime: TextView = itemView.findViewById(R.id.check_in_time)
        val startTime:TextView =itemView.findViewById(R.id.check_in_timeStart)
        val endTime:TextView =itemView.findViewById(R.id.check_in_timeEnd)
        val checkBtn:Button = itemView.findViewById(R.id.check_in_btn)
        val confirmTxt:TextView = itemView.findViewById(R.id.check_in_confirm)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckInViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_check_in_for_nuser, parent, false)

        return CheckInViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged", "DefaultLocale")
    override fun onBindViewHolder(holder: CheckInViewHolder, position: Int) {

        val currentItem = approvedJobList[position]

        holder.startTime.text = currentItem.startHr
        holder.endTime.text = currentItem.endHr
        holder.textViewJobTitle.text = currentItem.jobTitle
        holder.checkInTime.visibility = View.GONE
        holder.checkBtn.visibility = View.GONE
        holder.confirmTxt.visibility = View.GONE

        val uid = GetData.getCurrentUserId()
        val jobDb = FirebaseDatabase.getInstance().getReference("Job")
        val checkInDb = FirebaseDatabase.getInstance().getReference("NUserCheckIn").child(currentItem.jobId.toString())
        val bUserCheckInDb = FirebaseDatabase.getInstance().getReference("CheckInFromBUser").child(currentItem.jobId.toString())
        val today = GetData.getCurrentDateTime()
        val todayTime = GetData.getTimeFromString(today)
        val currentDayString = GetData.getDateFromString(today)
        val currentDay = GetData.formatDateForFirebase(currentDayString)
        jobDb.child(currentItem.buserId.toString()).child(currentItem.jobId.toString()).get().addOnSuccessListener { jobSnapShot->
            val jobStatus = jobSnapShot.child("status").getValue(String::class.java)
            val jobType = jobSnapShot.child("jobType").getValue(String::class.java)
            if(jobStatus!= null && jobStatus=="working" && jobType!= null) {
                // lấy dữ liệu từ fb về xem đã check in chưa
                checkInDb.child(currentDay).child(uid.toString()).get().addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val checkInStatus =
                            dataSnapshot.child("status").getValue(String::class.java).toString()
                        val checkInTime =
                            dataSnapshot.child("checkInTime").getValue(String::class.java).toString()
                        // kiểm tra buser đã xác nhận check in chưa
                        if(checkInStatus != "uncomfirmed checked in") {
                            // hiện tại sau endTime thì sẽ mở check out trong vòng 60 phút
                            if(CheckTime.calculateMinuteDiff(currentItem.endHr.toString(), todayTime) in 0..LATEST_TIME) {

                                val checkOutTime =
                                    dataSnapshot.child("checkOutTime").getValue(String::class.java).toString()
                                // kiểm tra check out time có rỗng không
                                if (checkOutTime == "") {
                                    if(CheckTime.areDatesEqual(currentItem.endTime.toString(), currentDayString)){
                                        holder.checkInTime.text = context.getString(R.string.check_out_notice)
                                        holder.checkInTime.visibility = View.VISIBLE
                                    }
                                    holder.checkBtn.setText(R.string.check_out)
                                    holder.checkBtn.visibility = View.VISIBLE
                                    holder.checkBtn.setOnClickListener {
                                        // lấy thời gian lúc ấn nút đểm làm thời gian chấm công
                                        val currentTime =
                                            GetData.getTimeFromString(GetData.getCurrentDateTime())
                                        setCheckedOutBtn(holder.checkBtn)
                                        // lấy số giờ làm việc
                                        val workHr = GetData.calculateHourDifference(checkInTime, currentItem.endHr.toString())
                                        var salary = workHr*currentItem.salary.toString().toFloat()

                                        // điểm danh muộn
                                        if(GetData.calculateHourDifference(currentItem.startHr.toString(), checkInTime) > 0){
                                            //giảm 15% lương
                                            salary *= LATE_MINUS_PERCENT
                                        }
                                        // lấy 2 số sau dấu .
                                        val stringSalary = salary.roundToInt().toString()

                                        val updateCheckOutTime = hashMapOf<String, Any>(
                                            "checkOutTime" to currentTime,
                                            "status" to "checked out",
                                            "salary" to stringSalary
                                        )

                                        checkInDb.child(currentDay).child(uid.toString())
                                            .updateChildren(updateCheckOutTime)

                                        bUserCheckInDb.child(currentDay).child(uid.toString())
                                            .updateChildren(updateCheckOutTime)

                                        FirebaseDatabase.getInstance().getReference("Salary")
                                            .child(currentItem.jobId.toString())
                                            .child(uid.toString()).get()
                                            .addOnSuccessListener { salarySnapshot->
                                                val totalSalary =salarySnapshot.child("totalSalary").getValue(Float::class.java)
                                                val workedDay = salarySnapshot.child("workedDay").getValue(Int::class.java)
                                                if(totalSalary!= null && workedDay!= null) {
                                                    val newTotalSalary = totalSalary + stringSalary.toFloat()

                                                    val newWorkedDay = workedDay +1

                                                    val updateSalary = hashMapOf<String, Any>(
                                                        "totalSalary" to newTotalSalary,
                                                        "workedDay" to newWorkedDay
                                                    )

                                                    FirebaseDatabase.getInstance().getReference("Salary")
                                                        .child(currentItem.jobId.toString())
                                                        .child(uid.toString()).updateChildren(updateSalary)
                                                }
                                            }

                                        // đẩy giá trị thu nhập lên firebase
                                        viewModel.pushIncomeToFirebaseByDate(uid.toString(), stringSalary, currentDayString)

                                        val jobTypeId = GetData.getIntFromJobType(jobType)

                                        viewModel.pushIncomeToFirebaseJobTypeId(uid.toString(),stringSalary, jobTypeId)

                                        //Đẩy giá trị giờ làm việc lên Firebase
                                        workHourViewModel.pushWorkHourToFirebase(uid.toString(), workHr.toString(), currentDayString)

                                    }
                                } else {
                                    if(CheckTime.areDatesEqual(currentItem.endTime.toString(), currentDayString)){
                                        holder.checkInTime.text = context.getString(R.string.check_out_notice)
                                        holder.checkInTime.visibility = View.VISIBLE
                                    }
                                    holder.checkBtn.visibility = View.VISIBLE
                                    setCheckedOutBtn(holder.checkBtn)
                                }
                            }else{
                                // có dữ liệu sẽ hiển thị nút check -> checked

                                setCheckInTime(checkInTime, currentItem.startHr.toString(), holder.checkInTime)
                                holder.confirmTxt.setText(R.string.confirmed)
                                holder.confirmTxt.visibility = View.VISIBLE
                                holder.checkBtn.visibility = View.VISIBLE
                                setCheckedInBtn(holder.checkBtn)
                            }
                        }else{
                            // có dữ liệu sẽ hiển thị nút check -> checked nhưng chưa được xác nhận sẽ hiển thị unconfirm
                            setCheckInTime(checkInTime, currentItem.startHr.toString(), holder.checkInTime)
                            holder.confirmTxt.setText(R.string.unconfirmed)
                            holder.confirmTxt.visibility = View.VISIBLE
                            holder.checkBtn.visibility = View.VISIBLE
                            setCheckedInBtn(holder.checkBtn)
                        }

                    } else {
                        // kiểm tra giờ hiện tại nêú trong khoảng trước startTime 15' đến sau startTime 60' thì sẽ hiên nút check in
                        if(CheckTime.calculateMinuteDiff(currentItem.startHr.toString(), todayTime) in -15.. LATEST_TIME) {
                            // khi không có dữ liệu sẽ cho phép ấn nút hoạt động
                            holder.checkBtn.visibility = View.VISIBLE
                            holder.checkBtn.setOnClickListener {
                                // lấy thời gian lúc ấn nút đểm làm thời gian điểm danh
                                var currentTime = GetData.getTimeFromString(GetData.getCurrentDateTime())
                                setCheckedInBtn(holder.checkBtn)
                                setCheckInTime(currentTime, currentItem.startHr.toString(), holder.checkInTime)
                                holder.confirmTxt.setText(R.string.unconfirmed)
                                holder.confirmTxt.visibility = View.VISIBLE

                                if(CheckTime.checkTimeBefore(currentTime, currentItem.startHr.toString())){
                                    currentTime = currentItem.startHr.toString()
                                }

                                val checkIn = CheckInFromBUserModel(uid.toString(), today, currentTime, "", "uncomfirmed checked in","0")

                                checkInDb.child(currentDay).child(uid.toString()).setValue(checkIn)
                            }
                        }
                    }
                }.addOnFailureListener{
                    // Handle failure here if necessary
                }

            }else{
                if(jobStatus != "closed") {
                    holder.checkInTime.setText(R.string.not_working_yet)
                    holder.checkInTime.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: MutableList<AppliedJobModel>) {
        approvedJobList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = approvedJobList.size

    private fun setCheckInTime(checkInTime:String, startTime:String, checkTimeTxt:TextView){
        if(CheckTime.checkTimeBefore(checkInTime, startTime)){
            checkTimeTxt.text = context.getText(R.string.in_time)
        }else{
            val lateMinute = CheckTime.calculateMinuteDiff(startTime, checkInTime)
            val lateMessage = "${context.getString(R.string.late)} $lateMinute ${context.getString(R.string.minute)}"
            checkTimeTxt.text = lateMessage
        }
        checkTimeTxt.visibility = View.VISIBLE
    }

    private fun setCheckedInBtn(checkBtn:Button){
        checkBtn.isClickable = false
        checkBtn.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context,
                R.color.gray
            )
        )
        checkBtn.setText(R.string.checked_in)
        checkBtn.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    private fun setCheckedOutBtn(checkBtn:Button){
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