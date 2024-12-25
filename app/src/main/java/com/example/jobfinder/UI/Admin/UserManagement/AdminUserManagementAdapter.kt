package com.example.jobfinder.UI.Admin.UserManagement

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.AdminModel.BasicInfoAndRole
import com.example.jobfinder.R
import com.example.jobfinder.Utils.RetriveImg

class AdminUserManagementAdapter(private var userList: List<BasicInfoAndRole>,
) :
    RecyclerView.Adapter<AdminUserManagementAdapter.UserManagementViewHolder>(), Filterable {

    private var filteredData: List<BasicInfoAndRole> = userList

    private var dataChangeListener: DataChangeListener? = null

    interface OnItemClickListener {
        fun onItemClick(userInfo: BasicInfoAndRole)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface DataChangeListener {
        fun onDataChanged(filteredList: List<BasicInfoAndRole>)
    }

    fun setDataChangeListener(listener: DataChangeListener) {
        dataChangeListener = listener
    }

    class UserManagementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.UM_username)
        val email: TextView = itemView.findViewById(R.id.email)
        val phone_num: TextView = itemView.findViewById(R.id.phone_num)
        val address: TextView = itemView.findViewById(R.id.address)
        val role: TextView = itemView.findViewById(R.id.role)
        val accStatus: TextView = itemView.findViewById(R.id.acc_status)
        val accStatusTitle: TextView = itemView.findViewById(R.id.acc_status_title)
        val avt: ImageView = itemView.findViewById(R.id.UM_user_avt)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserManagementViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_user_management_model, parent, false)

        return UserManagementViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserManagementViewHolder, position: Int) {
        val userInfo = userList[position]

        holder.userName.text = userInfo.userBasicInfo.name
        holder.email.text =
            "${holder.itemView.context.getString(R.string.email_title)} ${userInfo.userBasicInfo.email}"
        holder.phone_num.text = userInfo.userBasicInfo.phone_num
        holder.address.text = userInfo.userBasicInfo.address
        holder.accStatusTitle.text = "${holder.itemView.context.getString(R.string.status)}: "

        if (userInfo.userRole == "BUser") {
            holder.role.text = "${holder.itemView.context.getString(R.string.role_title)} ${
                holder.itemView.context.getString(R.string.buser)
            }"
        }
        if (userInfo.userRole == "NUser") {
            holder.role.text = "${holder.itemView.context.getString(R.string.role_title)} ${
                holder.itemView.context.getString(R.string.nuser)
            }"
        }
        if (userInfo.accountStatus == "active") {
            holder.accStatus.text = holder.itemView.context.getString(R.string.active)
            holder.accStatus.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.income_color
                )
            )
        } else {
            holder.accStatus.text = holder.itemView.context.getString(R.string.disable_acc)
            holder.accStatus.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.expense_color
                )
            )
        }

        RetriveImg.retrieveImage(userInfo.userBasicInfo.user_id.toString(), holder.avt)

        holder.itemView.setOnClickListener {
            listener?.onItemClick(userInfo)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BasicInfoAndRole>) {
        userList = newList
        filteredData = newList
        notifyDataSetChanged()
        dataChangeListener?.onDataChanged(newList)
    }

    override fun getItemCount() = userList.size

    // Lọc data khi search
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryInput = constraint?.toString()?.trim()?.lowercase() ?: ""
                val filteredList = mutableListOf<BasicInfoAndRole>()

                Log.d("Filter123", "Query: $queryInput")

                if (queryInput.isEmpty()) {
                    filteredList.addAll(filteredData)
                } else {
                    for (item in filteredData) {
                        val recName = item.userBasicInfo.name?.lowercase() ?: ""
                        val jobTitle = item.userBasicInfo.email?.lowercase() ?: ""
                        if (recName.contains(queryInput) || jobTitle.contains(queryInput)) {
                            filteredList.add(item)
                        }
                    }
                    Log.d("Filter123", "Filtered list size: ${filteredList.size}")
                }

                val filterResults = FilterResults()
                filterResults.count = filteredList.size
                filterResults.values = filteredList

                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val filteredList = results?.values as? List<BasicInfoAndRole> ?:listOf()
                userList = filteredList
                notifyDataSetChanged()

                Log.d("Filter123", "Publishing results, list size: ${filteredList.size}")

                dataChangeListener?.onDataChanged(filteredList)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun resetOriginalList() {
        userList = filteredData
        notifyDataSetChanged()
        dataChangeListener?.onDataChanged(filteredData)
    }

}