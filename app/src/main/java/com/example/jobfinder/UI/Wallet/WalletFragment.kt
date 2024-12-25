package com.example.jobfinder.UI.Wallet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinder.Datas.Model.WalletRowModel
import com.example.jobfinder.Datas.Model.walletAmountModel
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.FragmentWalletBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Currency

class WalletFragment(private val zaloPayment: RelativeLayout) : Fragment() {
    private lateinit var binding: FragmentWalletBinding
    private lateinit var walletAdapter: WalletAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var dataLoadListener: DataLoadListener
    lateinit var viewModel: WalletCardListViewModel

    // onAttach được gọi đầu tiên khi kết nối Fragment với Activity chứa nó
    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataLoadListener = context as? DataLoadListener ?: throw RuntimeException("$context must implement DataLoadListener")
    }


    // interface này cho phép com.example.jobfinder.UI.Wallet.WalletFragment gừi thông diệp cho WalletActivity khi load xong data
    interface DataLoadListener {
        fun onDataLoaded()
        fun onDataLoadedEmpty(isListEmpty: Boolean)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[WalletCardListViewModel::class.java]
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        zaloPayment.visibility= View.VISIBLE

        viewModel.fetchWalletAmount(uid.toString())

        viewModel.walletAmount.observe(viewLifecycleOwner){ amount->
            binding.amountInWalletAmount.text =amount
        }
        
        if(!viewModel.isDataLoaded) {
            FirebaseDatabase.getInstance()
                .getReference("Wallet")
                .child(uid.toString()).get()
                .addOnSuccessListener { dataSnapshot ->
                    for (cardSnapshot in dataSnapshot.children) {
                        // Truy cập trực tiếp các thuộc tính của WalletRowModel từ cardSnapshot
                        val cardId = cardSnapshot.child("cardId").getValue(String::class.java)
                        val bankName = cardSnapshot.child("bankName").getValue(String::class.java)
                        val amount = cardSnapshot.child("amount").getValue(String::class.java)
                        val cardNumber =
                            cardSnapshot.child("cardNumber").getValue(String::class.java)
                        val expDate = cardSnapshot.child("expDate").getValue(String::class.java)
                        val cardColor = cardSnapshot.child("cardColor").getValue(String::class.java)

                        // Tạo một đối tượng WalletRowModel từ các thuộc tính lấy được

                        val wallet =
                            WalletRowModel(cardId, bankName, amount, cardNumber, expDate, cardColor)
                        viewModel.addCardData(wallet)
                        viewModel.isDataLoaded = true
                        // gửi sự kiện qua dataLoadListener => thông qua interface ở bên trên => kích hoạt onDataLoaded() trong WalletActivity
                        dataLoadListener.onDataLoaded()
                    }
                    loadCardListData()

                }
                .addOnFailureListener { exception ->
                    Log.e("Wallet data", "Error getting data from Firebase", exception)
                }
        }else{
            loadCardListData()
        }

    }

    fun updateNoWalletCardVisibility(visibility: Int) {
        binding.noWalletCard.visibility = visibility
    }

    fun loadCardListData(){
        // Khởi tạo adapter và thiết lập RecyclerView
        val walletCardList = viewModel.getCardList()
        walletAdapter = WalletAdapter(
            walletCardList,
            requireContext(),
            binding.noWalletCard,
            viewModel
        ) { newAmount ->
            val format = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("VND")
            binding.amountInWalletAmount.setText(format.format(newAmount.toDouble()))
        }

        binding.recyclerWalletList.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerWalletList.adapter = walletAdapter
        // Gọi sự kiện đối với list empty tương tự như khi adapter có data
        dataLoadListener.onDataLoadedEmpty(walletCardList.isEmpty())
    }

    fun refreshContent() {
        val uid = GetData.getCurrentUserId()
        viewModel.fetchWalletAmount(uid.toString())
        viewModel.walletAmount.observe(viewLifecycleOwner){ amount->
            binding.amountInWalletAmount.text =amount
        }
    }
//    fun setDataLoadListener(listener: DataLoadListener) {
//        dataLoadListener = listener
//    }

}
