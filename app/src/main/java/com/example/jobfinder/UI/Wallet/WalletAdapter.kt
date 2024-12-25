package com.example.jobfinder.UI.Wallet

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Datas.Model.WalletRowModel
import com.example.jobfinder.Datas.Model.walletHistoryModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.RowWalletCardBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Currency
import kotlin.random.Random

class WalletAdapter(private val walletList: MutableList<WalletRowModel>,
                    private val context: Context,
                    private val noWalletLayout: ConstraintLayout,
                    private val viewModel: WalletCardListViewModel,
                    private val depositWithdrawListener: (newAmount: String) -> Unit
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {

    var isDialogShown = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val binding = RowWalletCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WalletViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val wallet = walletList[position]
        holder.bind(wallet)
    }

    override fun getItemCount(): Int {
        return walletList.size
    }

    inner class WalletViewHolder(private val binding: RowWalletCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(wallet: WalletRowModel) {
            binding.apply {
                // Binding dữ liệu từ wallet vào các view tương ứng
                txtWalletBankName.text = wallet.bankName

                // Chuyển sang vnd (chỉ hiển thị còn tính toán như bth)
                val format = NumberFormat.getCurrencyInstance()
                format.maximumFractionDigits = 0
                format.currency = Currency.getInstance("VND")
                walletAmount.setText(format.format(wallet.amount?.toDouble()))

                walletId.text = wallet.cardNumber
                imageMainWalletImage.setImageResource(randomMaskGroup())
                rowWallet.setBackgroundResource(getGradientDrawable(wallet.cardColor))
                // Thiết lập onClickListener cho thẻ
                rowWallet.setOnClickListener {
                    if (!isDialogShown) { // Kiểm tra trạng thái của dialog
                        showOptionsDialog(wallet)
                    }
                }

            }
        }

        private fun checkEmptyAdapter() {
            if (walletList.isEmpty()) {
                // Ẩn RecyclerView và hiển thị layout không có thẻ
                noWalletLayout.visibility = View.VISIBLE
            } else {
                // Hiển thị RecyclerView và ẩn layout không có thẻ
                noWalletLayout.visibility = View.GONE
            }
        }

        fun closeDialog() {
            // Đóng dialog và cập nhật trạng thái của biến isDialogShown
            // Ví dụ: dialog.dismiss()
            isDialogShown = false
        }

        // Hiển thị dialog với các chức năng
        private fun showOptionsDialog(wallet: WalletRowModel) {
            val dialog = Dialog(binding.root.context)
            val today = GetData.getCurrentDateTime()
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val walletAmountRef = FirebaseDatabase.getInstance().getReference("WalletAmount").child(uid)
            val walletRef = FirebaseDatabase.getInstance().getReference("Wallet").child(uid).child(wallet.cardId ?: "")
            val notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(uid)
            val walletHistoryRef= FirebaseDatabase.getInstance().getReference("WalletHistory").child(uid).child(wallet.cardId.toString())

            dialog.setContentView(R.layout.dialog_wallet_data)
            isDialogShown = true

            // Tìm kiếm các nút trong dialog
            val amountEditTxt = dialog.findViewById<TextInputEditText>(R.id.amount)
            val withdrawBtn = dialog.findViewById<Button>(R.id.withdraw_cash)
            val depositBtn = dialog.findViewById<Button>(R.id.deposit_cash)
            val deleteButton = dialog.findViewById<Button>(R.id.delete_card_button)
            val addCashToCardBtn = dialog.findViewById<Button>(R.id.add_cash_to_card)
            val cancelButton = dialog.findViewById<Button>(R.id.button_cancel)
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("VND")

            addCashToCardBtn.isClickable= true
            depositBtn.isClickable= true
            withdrawBtn.isClickable= true
            deleteButton.isClickable= true

            withdrawBtn.setOnClickListener {
                val amountTxt = amountEditTxt.text.toString().trim()
                val isValidAmountTxt = VerifyField.isEmpty(amountTxt)
                amountEditTxt.error = if(isValidAmountTxt) null else getString(binding.root.context, R.string.no_amount)

                if(isValidAmountTxt){
                    walletAmountRef.get().addOnSuccessListener { data ->
                        if (data.exists()) {
                            val currentWalletAmount = data.child("amount").getValue(String::class.java).toString()
                            if (GetData.compareFloatStrings(currentWalletAmount, amountTxt)) {
                                depositBtn.isClickable= false
                                withdrawBtn.isClickable= false
                                addCashToCardBtn.isClickable= false
                                deleteButton.isClickable= false
                                // Trừ số tiền từ số dư ví
                                val newAmount = (wallet.amount.toString().toFloat() + amountTxt.toFloat()).toString()
                                val newWalletAmount = (currentWalletAmount.toFloat() - amountTxt.toFloat()).toString()
                                walletAmountRef.child("amount").setValue(newWalletAmount)

                                Toast.makeText(binding.root.context, getString(binding.root.context, R.string.withdraw_success), Toast.LENGTH_SHORT).show()

                                // Cập nhật số dư trong thẻ
                                walletRef.child("amount")
                                    .setValue(newAmount)

                                //update fragment
                                depositWithdrawListener.invoke(newWalletAmount)
                                wallet.amount = newAmount
                                bind(wallet)

                                // noti
                                val notiId = notiRef.push().key.toString()
                                val amountConvertVnd = format.format(amountTxt.toDouble())
                                val newNoti = NotificationsRowModel(notiId, "Admin",
                                    "${getString(binding.root.context, R.string.withdraw)}: $amountConvertVnd \n" +
                                            "${getString(binding.root.context, R.string.bank_name)}: ${wallet.bankName} \n " +
                                            "${getString(binding.root.context, R.string.card_number)}: ${wallet.cardNumber}",
                                    today)
                                notiRef
                                    .child(notiId)
                                    .setValue(newNoti)
                                //wallet history
                                val walletHistoryId= walletHistoryRef.push().key.toString()
                                val walletHistoryModel= walletHistoryModel(
                                    walletHistoryId,
                                    amountTxt,
                                    wallet.cardId.toString(),
                                    wallet.bankName.toString(),
                                    wallet.cardNumber.toString(),
                                    today,
                                    "income")
                                walletHistoryRef
                                    .child(walletHistoryId)
                                    .setValue(walletHistoryModel)

                                dialog.dismiss()
                                isDialogShown = false
                            } else {
                                // Số dư trong ví không đủ
                                Toast.makeText(binding.root.context, getString(binding.root.context, R.string.not_enough_money), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Xử lý khi không tìm thấy dữ liệu trong nút "WalletAmount"
                        }
                    }.addOnFailureListener {
                        // Xử lý khi có lỗi xảy ra khi truy vấn dữ liệu từ Firebase
                    }
                }
            }

            depositBtn.setOnClickListener {
                val amountTxt = amountEditTxt.text.toString().trim()
                val isValidAmountTxt = VerifyField.isEmpty(amountTxt)
                amountEditTxt.error = if (isValidAmountTxt) null else getString(binding.root.context, R.string.no_amount)

                if (isValidAmountTxt) {
                    walletAmountRef.get().addOnSuccessListener { data ->
                        if (data.exists()) {
                            val currentWalletAmount = data.child("amount").getValue(String::class.java).toString()
                            if (GetData.compareFloatStrings(wallet.amount.toString(), amountTxt)) {
                                depositBtn.isClickable= false
                                withdrawBtn.isClickable= false
                                addCashToCardBtn.isClickable= false
                                deleteButton.isClickable= false
                                // Trừ số tiền từ số dư ví
                                val newAmount = (wallet.amount.toString().toFloat() - amountTxt.toFloat()).toString()
                                val newWalletAmount = (currentWalletAmount.toFloat() + amountTxt.toFloat()).toString()
                                walletAmountRef.child("amount").setValue(newWalletAmount)

                                Toast.makeText(binding.root.context, getString(binding.root.context, R.string.deposit_success), Toast.LENGTH_SHORT).show()
                                // Cập nhật số dư trong thẻ
                                walletRef.child("amount")
                                    .setValue(newAmount)

                                // update in fragment
                                wallet.amount = newAmount
                                bind(wallet)
                                depositWithdrawListener.invoke(newWalletAmount)

                                // noti
                                val notiId = notiRef.push().key.toString()
                                val amountConvertVnd = format.format(amountTxt.toDouble())
                                val newNoti = NotificationsRowModel(notiId, "Admin",
                                    "${getString(binding.root.context, R.string.deposit)} : ${amountConvertVnd} \n" +
                                            "${getString(binding.root.context, R.string.bank_name)}: ${wallet.bankName} \n" +
                                            " ${getString(binding.root.context, R.string.card_number)}: ${wallet.cardNumber}",
                                    today)
                                notiRef
                                    .child(notiId)
                                    .setValue(newNoti)
                                //wallet history
                                val walletHistoryId= walletHistoryRef.push().key.toString()
                                val walletHistoryModel= walletHistoryModel(
                                    walletHistoryId,
                                    amountTxt,
                                    wallet.cardId.toString(),
                                    wallet.bankName.toString(),
                                    wallet.cardNumber.toString(),
                                    today,
                                    "expense")
                                walletHistoryRef
                                    .child(walletHistoryId)
                                    .setValue(walletHistoryModel)

                                dialog.dismiss()
                                isDialogShown = false
                            } else {
                                // Số dư trong ví không đủ
                                Toast.makeText(binding.root.context, getString(binding.root.context, R.string.not_enough_money), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Xử lý khi không tìm thấy dữ liệu trong nút "WalletAmount"
                        }
                    }.addOnFailureListener {
                        // Xử lý khi có lỗi xảy ra khi truy vấn dữ liệu từ Firebase
                    }
                }
            }

            // Xử lý khi nhấn vào nút Xóa
            deleteButton.setOnClickListener {
                // Xóa thẻ
                deleteWallet(wallet)
                dialog.dismiss() // Đóng dialog
                isDialogShown = false
            }

            // Xử lý khi nhấn vào nút Hủy
            cancelButton.setOnClickListener {
                dialog.dismiss() // Đóng dialog
                isDialogShown = false
            }

            // Xử lý khi nhấn vào nút Thêm
            addCashToCardBtn.setOnClickListener {
                addCashToCardBtn.isClickable= false
                // Thêm 10000000 vào amount của thẻ
                addMoney(wallet)
                    walletRef.child("amount")
                        .setValue(wallet.amount)
                        .addOnSuccessListener {
                            val notiId = notiRef.push().key.toString()
                            val walletHistoryId= walletHistoryRef.push().key.toString()
                            val walletHistoryModel= walletHistoryModel(
                                walletHistoryId,
                                "10000000",
                                wallet.cardId.toString(),
                                wallet.bankName.toString(),
                                wallet.cardNumber.toString(),
                                today,
                                "income")
                            val notificationsRowModel= NotificationsRowModel(
                                notiId,
                                "Admin",
                                "+ 10.000.000 đ\n" +
                                        "${getString(binding.root.context, R.string.bank_name)}: ${wallet.bankName}. ${getString(binding.root.context, R.string.card_number)}: ${wallet.cardNumber}",
                                today
                            )
                            //add to WalletHistory
                            walletHistoryRef
                                .child(walletHistoryId)
                                .setValue(walletHistoryModel)
                            //add to Notifications
                            notiRef
                                .child(notiId)
                                .setValue(notificationsRowModel)
                            Toast.makeText(context, getString(context,R.string.add_cash_success), Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            // Xử lý khi thêm thất bại
                            Log.e("Add Money", "Error adding money to Firebase", exception)
                        }
                dialog.dismiss() // Đóng dialog // Đóng dialog
                isDialogShown = false

            }

            dialog.setOnDismissListener {
                isDialogShown = false
            }

            dialog.show() // Hiển thị dialog
        }

        private fun deleteWallet(wallet: WalletRowModel) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Remove wallet from RecyclerView
                walletList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, walletList.size - position)
                checkEmptyAdapter()

                // Remove wallet from Firebase
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                uid?.let { userId ->
                    FirebaseDatabase.getInstance().getReference("Wallet").child(userId)
                        .child(wallet.cardId ?: "")
                        .removeValue()
                        .addOnSuccessListener {
                            // Handle success
                            FirebaseDatabase.getInstance().getReference("WalletHistory").child(userId)
                                .child(wallet.cardId ?: "")
                                .removeValue()
                            Toast.makeText(context, getString(context,R.string.delete_card_success), Toast.LENGTH_SHORT).show()

                            // Update ViewModel after successful deletion
                            viewModel.removeCardData(wallet)
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                            Log.e("WalletAdapter", "Error deleting wallet from Firebase", exception)
                        }
                }
            }
        }


        // Hàm thêm tiền vào thẻ
        private fun addMoney(wallet: WalletRowModel) {
            // Chuyển đổi giá trị amount từ string thành kiểu float
            val currentAmount = wallet.amount?.toFloatOrNull() ?: 0f
            // Thêm 10000000 vào giá trị hiện tại
            val newAmount = currentAmount + 10000000f
            // Cập nhật giá trị amount của thẻ thành chuỗi mới
            wallet.amount = newAmount.toString()
            // Cập nhật lại giao diện cho thẻ
            bind(wallet)
        }

        private fun getGradientDrawable(cardColor: String?): Int {
            return when (cardColor) {
                "red" -> R.drawable.wallet_red_bg
                "green" -> R.drawable.wallet_green_bg
                "blue" -> R.drawable.wallet_blue_bg
                "pink"-> R.drawable.wallet_pink_bg
                else -> R.drawable.wallet_blue_bg
            }
        }

        private fun randomMaskGroup(): Int {
            val num = Random.nextInt(1, 4)
            return when (num) {
                1-> R.drawable.img_mask_group
                2-> R.drawable.img_mask_group_white_a700
                3-> R.drawable.img_mask_group_white_a700_170x319
                else -> {R.drawable.img_mask_group}
            }
        }
        
    }
}
