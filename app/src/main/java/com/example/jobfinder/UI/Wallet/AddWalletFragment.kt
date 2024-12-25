package com.example.jobfinder.UI.Wallet

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.Datas.Model.WalletRowModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.PreventDoubleClick
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentAddWalletBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddWalletFragment(private val zaloPayment: RelativeLayout) : Fragment() {
    private lateinit var binding: FragmentAddWalletBinding
    private var pickedColor = "blue"
    private lateinit var auth: FirebaseAuth
    private var yearChoose= false
    private var monthChoose=false
    private var bankChoosed= false
    private var validCard= true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddWalletBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validCard= true

        //firebase
        auth = FirebaseAuth.getInstance()

        binding.imageMainAddWalletImage.setImageResource(R.drawable.img_mask_group)

        zaloPayment.visibility= View.GONE

        binding.txtBank.isClickable = true
        binding.addCardBtn.isClickable = true
        binding.txtMonth.isClickable = true
        binding.txtYear.isClickable = true
        binding.addWalletCardNumEditTxt.isClickable = true

        binding.txtBank.setOnClickListener{
            if(!bankChoosed){
                binding.txtBank.text = "Agribank"
                binding.txtBank.error= null}
            bankSelect(it)
            bankChoosed = true
        }
        binding.txtYear.setOnClickListener {
            if(!yearChoose){
                binding.txtYear.text = "25"
                binding.txtYear.error= null}
            txtYearSelected(it)
            yearChoose = true
        }
        binding.txtMonth.setOnClickListener{
            if(!monthChoose){
                binding.txtMonth.text = "01"
                binding.txtMonth.error = null
            }
            txtMonthSelected(it)
            monthChoose = true
        }

        binding.addCardBtn.setOnClickListener{

            val bankName = binding.txtBank.text.toString()
            val cardNumber = binding.addWalletCardNumEditTxt.text.toString().trim()
            val expYear = binding.txtYear.text.toString()
            val expMonth = binding.txtMonth.text.toString()
            val expDate = "$expYear/$expMonth"

            val isValidBank= bankChoosed
            val isValidCardNumber= VerifyField.isValidCardNumber(cardNumber)
            val isValidExpDate= VerifyField.isEmpty(expDate)
            val isValidYear= yearChoose
            val isValidMonth= monthChoose

            binding.txtBank.error = if (isValidBank) null else getString(R.string.no_choose_bank)
            binding.addWalletCardNumEditTxt.error = if (isValidCardNumber) null else getString(R.string.error_invalid_card_num)
            binding.txtYear.error=if(isValidYear)null else getString(R.string.no_choose_year)
            binding.txtMonth.error=if(isValidMonth)null else getString(R.string.no_choose_month)

            // Tạo một coroutine mới
            lifecycleScope.launch {
                val isValidCard = validCard(bankName, cardNumber)
                if (isValidCard) {
                    // Thẻ hợp lệ, tiếp tục xử lý
                    if(isValidBank && isValidCardNumber &&isValidExpDate && isValidYear&& isValidMonth){
                        binding.txtBank.isClickable = false
                        binding.addCardBtn.isClickable = false
                        binding.txtMonth.isClickable = false
                        binding.txtYear.isClickable = false
                        binding.addWalletCardNumEditTxt.isClickable = false
                        if (PreventDoubleClick.checkClick()) {
                            val cardColor = pickedColor
                            val uid = auth.currentUser?.uid
                            val cardId= FirebaseDatabase.getInstance().getReference("Wallet").child(uid.toString()).push().key
                            val newWalletRow = WalletRowModel(cardId,bankName, "0.0", cardNumber, expDate, cardColor)
                            FirebaseDatabase
                                .getInstance()
                                .getReference("Wallet")
                                .child(uid.toString())
                                .child(cardId.toString())
                                .setValue(newWalletRow)
                                .addOnCompleteListener {
                                    if(it.isSuccessful){
                                        val notiId = FirebaseDatabase
                                            .getInstance()
                                            .getReference("Notifications")
                                            .child(uid.toString()).push().key.toString()
                                        val today = GetData.getCurrentDateTime()
                                        val notificationsRowModel= NotificationsRowModel(
                                            notiId,
                                            "Admin",
                                            "${getString(R.string.add_card_to_user_wallet)}.\n" +
                                                    "${getString(R.string.bank_name)}: $bankName. ${getString(R.string.card_number)}: $cardNumber",
                                            today)
                                        FirebaseDatabase.getInstance()
                                            .getReference("Notifications")
                                            .child(uid.toString())
                                            .child(notiId)
                                            .setValue(notificationsRowModel)
                                        Toast.makeText(context, getString(R.string.add_card_success), Toast.LENGTH_SHORT).show()
                                        val intent = Intent(activity, WalletActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        startActivity(intent)
                                    }else {
                                        Toast.makeText(context, getString(R.string.add_card_fail), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                    else{
                        checkToAutoFocus(isValidBank, isValidCardNumber,isValidExpDate)
                    }
                } else {
                    Toast.makeText(context, getString(R.string.duplicate_card), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.chooseColorBlue.setOnClickListener{
            pickedColor = "blue"
            binding.apply {
                addWalletCardPreviewBg.setBackgroundResource(R.drawable.wallet_blue_bg)
                imageMainAddWalletImage.setImageResource(R.drawable.img_mask_group)
            }
        }
        binding.chooseColorRed.setOnClickListener{
            pickedColor = "red"
            binding.apply {
                addWalletCardPreviewBg.setBackgroundResource(R.drawable.wallet_red_bg)
                imageMainAddWalletImage.setImageResource(R.drawable.img_mask_group_white_a700)
            }
        }
        binding.chooseColorGreen.setOnClickListener{
            pickedColor = "green"
            binding.apply {
                addWalletCardPreviewBg.setBackgroundResource(R.drawable.wallet_green_bg)
                imageMainAddWalletImage.setImageResource(R.drawable.img_mask_group_white_a700_170x319)
            }
        }
        binding.chooseColorPink.setOnClickListener{
            pickedColor = "pink"
            binding.apply {
                addWalletCardPreviewBg.setBackgroundResource(R.drawable.wallet_pink_bg)
                imageMainAddWalletImage.setImageResource(R.drawable.img_mask_group)
            }
        }
    }

    private suspend fun validCard(cardBank: String, cardNum: String): Boolean {

        val dataSnapshot = try {
            FirebaseDatabase.getInstance()
                .getReference("Wallet")
                .get()
                .await()
        } catch (e: Exception) {
            return false
        }
        for(user in dataSnapshot.children) {
            for (cardSnapshot in user.children) {
                val bankName = cardSnapshot.child("bankName").getValue(String::class.java)
                val cardNumber = cardSnapshot.child("cardNumber").getValue(String::class.java)

                if (cardBank == bankName && cardNum == cardNumber) {
                    return false
                }
            }
        }

        return true
    }

    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<EditText>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.addWalletCardNumEditTxt)
                }
            }
        }

        if (invalidFields.isNotEmpty()) {
            invalidFields.first().requestFocus()
        }
    }

    private fun showPopupMenu(view: View, menuResId: Int, itemClickListener: (String) -> Unit) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemTitle = menuItem.title.toString()
            itemClickListener.invoke(itemTitle)
            true
        }
        popupMenu.show()
    }

    private fun txtYearSelected(view: View) {
        showPopupMenu(view, R.menu.year_menu) { selectedYear ->
            binding.txtYear.text = selectedYear
        }
    }

    private fun bankSelect(view: View) {
        showPopupMenu(view, R.menu.bank_menu) { selectedBank ->
            binding.txtBank.text = selectedBank
        }
    }

    private fun txtMonthSelected(view: View) {
        showPopupMenu(view, R.menu.month_menu) { selectedMonth ->
            binding.txtMonth.text = selectedMonth
        }
    }

}