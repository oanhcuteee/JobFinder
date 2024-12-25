package com.example.jobfinder.UI.FindNewJobs

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.JobModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import java.util.Currency
import java.util.Locale

class NewJobsAdapter(
    private var list: List<JobModel>,
) : RecyclerView.Adapter<NewJobsAdapter.NewJobViewHolder>(), Filterable {

    lateinit var mListener: onItemClickListener
    private var originalData: List<JobModel> = list
    private var filteredData: List<JobModel> = list
    private var dataChangeListener: DataChangeListener? = null


    interface onItemClickListener {
        fun onItemClicked(Job: JobModel) {}
    }

    interface DataChangeListener {
        fun onDataChanged(filteredList: List<JobModel>)
    }

    fun setDataChangeListener(listener: DataChangeListener) {
        dataChangeListener = listener
    }

    // Click vào từng item
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    inner class NewJobViewHolder(view: View, listener: onItemClickListener) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView
        val rec_nameNJ: TextView
        val jobTitle: TextView
        val numOfRecruits: TextView
        val numOfRecruited: TextView
        val workTimeStart: TextView
        val workTimeEnd: TextView
        val appliDeadline: TextView
        val postedTime: TextView
        val salary: TextView


        init {
            avatar = view.findViewById(R.id.user_ava)
            rec_nameNJ = view.findViewById(R.id.rec_nameNJ)
            jobTitle = view.findViewById(R.id.JiPosition2)
            numOfRecruits = view.findViewById(R.id.NumOfRecruits)
            numOfRecruited = view.findViewById(R.id.NumOfRecruited)
            workTimeStart = view.findViewById(R.id.timeStart)
            workTimeEnd = view.findViewById(R.id.timeEnd)
            appliDeadline = view.findViewById(R.id.appliDeadline)
            postedTime = view.findViewById(R.id.posttime)
            salary = view.findViewById(R.id.salary)

            view.setOnClickListener {
                val position = bindingAdapterPosition // Lấy vị trí của item trong danh sách
                if (position != RecyclerView.NO_POSITION) {
                    val job = list[position] // Lấy JobModel tương ứng với vị trí
                    listener.onItemClicked(job) // Gọi phương thức onItemClicked với JobModel
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewJobsAdapter.NewJobViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.new_jobs_item_home, parent, false)
        return NewJobViewHolder(view, mListener)
    }

    // gán data vào từng phần tử trong item (gán data trong Model.JobModel vào textview)
    override fun onBindViewHolder(holder: NewJobViewHolder, position: Int) {
        // Định dạng giá trị salary với dấu phẩy VNĐ
        val format = java.text.NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance("VND")

//        holder.avatar.setImageResource(list[position].avatar)
        RetriveImg.retrieveImage(list[position].BUserId.toString(), holder.avatar)
        holder.rec_nameNJ.text = list[position].BUserName?.uppercase(Locale.getDefault())
        holder.jobTitle.text = list[position].jobTitle
        holder.numOfRecruits.text = list[position].empAmount
        holder.numOfRecruited.text = list[position].numOfRecruited
        holder.workTimeStart.text = list[position].startHr
        holder.workTimeEnd.text = list[position].endHr
        holder.appliDeadline.text = list[position].endTime
        holder.postedTime.text = GetData.getDateFromString(list[position].postDate.toString())
        holder.salary.text = format.format(list[position].salaryPerEmp?.toDouble())

    }

    override fun getItemCount(): Int = list.size


    // Lọc data khi search
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryInput = constraint?.toString()?.trim()?.lowercase() ?: ""
                val filteredList = mutableListOf<JobModel>()

                if (queryInput.isEmpty()) {
                    filteredList.addAll(filteredData)
                } else {
                    for (item in filteredData) {
                        val recName = item.BUserName?.lowercase() ?: ""
                        val jobTitle = item.jobTitle?.lowercase() ?: ""
                        if (recName.contains(queryInput) || jobTitle.contains(queryInput)) {
                            filteredList.add(item)
                        }
                    }
                }

                val filterResults = FilterResults()
                filterResults.count = filteredList.size
                filterResults.values = filteredList

                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filteredList = results?.values as? List<JobModel> ?: emptyList()
                list = filteredList
                notifyDataSetChanged()
                dataChangeListener?.onDataChanged(filteredList)
            }
        }
    }




    @SuppressLint("NotifyDataSetChanged")
    fun resetOriginalList() {
        list = filteredData
        notifyDataSetChanged()
        dataChangeListener?.onDataChanged(filteredData)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<JobModel>) {
        list = newList
        filteredData = newList
        notifyDataSetChanged()
        dataChangeListener?.onDataChanged(newList)
    }

}