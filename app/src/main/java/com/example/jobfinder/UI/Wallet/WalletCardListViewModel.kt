package com.example.jobfinder.UI.Wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.WalletRowModel
import com.example.jobfinder.Utils.GetData
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Currency

class WalletCardListViewModel: ViewModel() {

    val database = FirebaseDatabase.getInstance().getReference("WalletAmount")
    private val _walletAmount = MutableLiveData<String>()
    val walletAmount: LiveData<String> = _walletAmount

    private val cardList: MutableList<WalletRowModel> = mutableListOf()
    var isDataLoaded: Boolean = false

    // Thêm dữ liệu vào JobsList
    fun addCardData(cardData: WalletRowModel) {
        cardList.add(cardData)
    }

    // Lấy danh sách dữ liệu cho adapter.
    fun getCardList(): MutableList<WalletRowModel> {
        return cardList
    }

    fun removeCardData(wallet: WalletRowModel) {
        cardList.remove(wallet)
    }

    fun fetchWalletAmount(uid:String){
        database.child(uid).get()
            .addOnSuccessListener { data ->
                var walletAmount = ""
                if (data.exists()) {
                    val amount = data.child("amount").getValue(String::class.java)
                    // Chuyển sang vnd (chỉ hiển thị còn tính toán như bth)
                    val format = NumberFormat.getCurrencyInstance()
                    format.maximumFractionDigits = 0
                    format.currency = Currency.getInstance("VND")
                    walletAmount = format.format(amount?.toDouble())
                }
                _walletAmount.value = walletAmount
            }
    }

}