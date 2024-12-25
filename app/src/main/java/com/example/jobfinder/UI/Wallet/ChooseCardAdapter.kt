package com.example.jobfinder.UI.Wallet

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.jobfinder.Datas.Model.bankAndCardNumModel
import com.example.jobfinder.R

class ChooseCardAdapter(context: Context, private val cardList: MutableList<bankAndCardNumModel>) :
    ArrayAdapter<bankAndCardNumModel>(context, android.R.layout.simple_spinner_dropdown_item, cardList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    @SuppressLint("SetTextI18n")
    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.spinner_items, parent, false)

        val bankAndCardNumModel = cardList[position]

        val bank = itemView.findViewById<TextView>(R.id.spinner_bank)
        val cardNum = itemView.findViewById<TextView>(R.id.spinner_card_num)
        bank.text = "${bankAndCardNumModel.bankName}"
        cardNum.text = "${bankAndCardNumModel.cardNum}"

        return itemView
    }
}
