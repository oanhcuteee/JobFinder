package com.example.jobfinder.UI.JobPosts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.jobfinder.R
class JobTypeSpinner(context: Context, private val dataList: Array<String>) :
    ArrayAdapter<String>(context, R.layout.spinner_job_type, dataList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.spinner_job_type, parent, false)
        val textView = itemView.findViewById<TextView>(R.id.spinner_bank)
        textView.text = dataList[position]
        return itemView
    }
}