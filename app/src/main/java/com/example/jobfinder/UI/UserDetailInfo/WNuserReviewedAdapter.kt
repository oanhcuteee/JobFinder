package com.example.jobfinder.UI.UserDetailInfo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.JobHistoryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg

class WNuserReviewedAdapter(
    private var list: List<JobHistoryModel>
) : RecyclerView.Adapter<WNuserReviewedAdapter.WNuserReviewedAdapterViewHolder>(){

    inner class WNuserReviewedAdapterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView
        val buserName: TextView
        val rating: RatingBar
        val jobTitle: TextView
        val rvContent: TextView
        val reviewDay: TextView

        init {
            avatar = view.findViewById(R.id.user_ava)
            buserName = view.findViewById(R.id.busername)
            rating = view.findViewById(R.id.ratingID_item)
            jobTitle = view.findViewById(R.id.exJob_position)
            rvContent = view.findViewById(R.id.RV_desc)
            reviewDay = view.findViewById(R.id.reviewDay)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WNuserReviewedAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_wnuser_reviewed, parent, false)
        return WNuserReviewedAdapterViewHolder(view)
    }

    // gán data vào từng phần tử trong item
    override fun onBindViewHolder(holder: WNuserReviewedAdapterViewHolder, position: Int) {
        RetriveImg.retrieveImage(list[position].BUserId.toString(), holder.avatar)
        holder.buserName.text = list[position].bUserName
        holder.rating.rating = list[position].rating?.toFloatOrNull() ?: 0.0f
        holder.jobTitle.text = list[position].jobTitle
        holder.rvContent.text = list[position].review
        holder.reviewDay.text = GetData.getDateFromString(list[position].endDate.toString())

    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<JobHistoryModel>) {
        list = newList
        notifyDataSetChanged()
    }

}
