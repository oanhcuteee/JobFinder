package com.example.jobfinder.UI.Wallet

import WalletHistoryAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.bankAndCardNumModel
import com.example.jobfinder.databinding.ActivityWalletHistoryBinding
import com.example.jobfinder.Datas.Model.walletHistoryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class WalletHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalletHistoryBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        FirebaseDatabase.getInstance()
            .getReference("WalletHistory")
            .child(uid.toString())
            .get()
            .addOnSuccessListener { dataSnapshot ->
                // Chuyển đổi dataSnapshot thành danh sách walletHistoryModel
                val cardList = mutableListOf<bankAndCardNumModel>()
                cardList.add(0, bankAndCardNumModel(resources.getString(R.string.all_card), ""))
                val walletHistoryList = dataSnapshot.children.flatMap { cardId ->
                    cardId.children.map { historySnapshot ->
                        val historyId = historySnapshot.child("historyId").getValue(String::class.java)
                        val amount = historySnapshot.child("amount").getValue(String::class.java)
                        val cardId = historySnapshot.child("cardId").getValue(String::class.java)
                        val bankName = historySnapshot.child("bankName").getValue(String::class.java)
                        val cardNum = historySnapshot.child("cardNum").getValue(String::class.java)
                        val date = historySnapshot.child("date").getValue(String::class.java)
                        val type = historySnapshot.child("type").getValue(String::class.java)

                        val bankAndCardNum = bankAndCardNumModel(bankName, cardNum)
                        if (!cardList.contains(bankAndCardNum)) {
                            cardList.add(bankAndCardNum)
                        }

                        walletHistoryModel(historyId, amount, cardId, bankName, cardNum, date, type)
                    }
                }
                if(walletHistoryList.isNotEmpty()){

                    val spinnerAdapter = ChooseCardAdapter(binding.root.context, cardList)
                    binding.historySpinner.adapter= spinnerAdapter

                    // Xử lý tương tác Spinner
                    binding.historySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            // Lấy thẻ được chọn từ danh sách
                            val selectedCard = cardList[position]

                            if (selectedCard.bankName == resources.getString(R.string.all_card)) {
                                // Hiển thị tất cả lịch sử ví nếu người dùng chọn "Tất cả thẻ"
                                val sortedList = walletHistoryList.sortedByDescending { GetData.convertStringToDate( it.date.toString()) }
                                val adapter = WalletHistoryAdapter(sortedList)
                                binding.recyclerWalletHistoryList.layoutManager = LinearLayoutManager(this@WalletHistoryActivity)
                                binding.recyclerWalletHistoryList.adapter = adapter
                            } else {
                                // Lọc lịch sử ví để chỉ hiển thị lịch sử ví của thẻ được chọn
                                val filteredHistoryList = walletHistoryList.filter { it.bankName == selectedCard.bankName && it.cardNum == selectedCard.cardNum }
                                val filteredSortedHistoryList = filteredHistoryList.sortedByDescending { GetData.convertStringToDate( it.date.toString()) }
                                // Hiển thị danh sách lịch sử ví được lọc trong RecyclerView
                                val adapter = WalletHistoryAdapter(filteredSortedHistoryList)
                                binding.recyclerWalletHistoryList.layoutManager = LinearLayoutManager(this@WalletHistoryActivity)
                                binding.recyclerWalletHistoryList.adapter = adapter
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Xử lý khi không có gì được chọn
                        }
                    }

                }else{
                    binding.noWalletHistory.visibility= View.VISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Wallet data", "Error getting data from Firebase", exception)
            }

        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

}