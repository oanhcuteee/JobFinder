package com.example.jobfinder.UI.AboutUs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.R

class AboutUsAdapter(private val pages: List<AboutPage>) : RecyclerView.Adapter<AboutUsAdapter.AboutPageViewHolder>() {

    data class AboutPage(val imageRes: Int, val title: String, val description: String)

    inner class AboutPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val title: TextView = itemView.findViewById(R.id.title)
        val description: TextView = itemView.findViewById(R.id.description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_aboutus_template, parent, false)
        return AboutPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: AboutPageViewHolder, position: Int) {
        val page = pages[position]
        holder.imageView.setImageResource(page.imageRes)
        holder.title.text = page.title
        holder.description.text = page.description
    }

    override fun getItemCount(): Int = pages.size
}
