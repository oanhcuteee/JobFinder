package com.example.jobfinder.UI.Admin.UserManagement

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.Datas.Model.NotificationsRowModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.PostedJob.PostedJobViewModel
import com.example.jobfinder.UI.UsersProfile.ProfileViewModel
import com.example.jobfinder.UI.Wallet.WalletCardListViewModel
import com.example.jobfinder.UI.Wallet.WalletFragment
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.ActivityNuserDetailInfoBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Currency

class AdminUMNUserDetail : AppCompatActivity() {
    private lateinit var binding: ActivityNuserDetailInfoBinding

    private val viewModel: ProfileViewModel by viewModels()
    private val walletViewModel : PostedJobViewModel by viewModels()
    private val UMViewModel:AdminUserManagementViewModel by viewModels()
    private val amountVM: WalletCardListViewModel by viewModels()
    private var clicked = false

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuserDetailInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance().reference
        val userId = intent.getStringExtra("uid")

        val accStatus = intent.getStringExtra("accStatus")

        if (accStatus != null){
            binding.amountWrapper.visibility = View.VISIBLE
            amountVM.fetchWalletAmount(userId.toString())

            amountVM.walletAmount.observe(this){ amount->
                binding.amountInWalletAmount.text =amount
            }
            if (accStatus == "active"){
                binding.approveBtn.text = getString(R.string.disable_acc)
                clicked = false
            }else{
                binding.approveBtn.text = getString(R.string.enable_acc)
                clicked = true
            }
        }
        binding.desHolder.visibility = View.GONE
        binding.recyclerHolder.visibility = View.GONE
        binding.rejectBtn.text = getString(R.string.add_cash_to_wallet_btn)

        binding.animationView.visibility = View.VISIBLE

        if (userId != null) {
            setupUserInformation(database, userId)
            setupButtons(userId)
        }

    }

    override fun onResume() {
        super.onResume()
        RetriveImg.retrieveImage(viewModel.userid, binding.profileImage)
    }

    private fun setupUserInformation(database: DatabaseReference, userId: String) {
        database.child("UserBasicInfo").child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.child("name").getValue(String::class.java)
                userName?.let {
                    viewModel.name = it
                    binding.editProfileName.setText(viewModel.name)
                }
                val email = snapshot.child("email").getValue(String::class.java)
                email?.let {
                    viewModel.email = it
                    binding.editProfileEmail.setText(viewModel.email)
                }
                val phone = snapshot.child("phone_num").getValue(String::class.java)
                phone?.let {
                    viewModel.phone = it
                    binding.editProfilePhonenum.setText(viewModel.phone)
                }
                val address = snapshot.child("address").getValue(String::class.java)
                address?.let {
                    viewModel.address = it
                    binding.editProfileAddress.setText(viewModel.address)
                }

                binding.animationView.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
            }
        })

        database.child("NUserInfo").child(userId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val age = snapshot.child("age").getValue(String::class.java)
                age?.let {
                    viewModel.age = it
                    if (it == "") {
                        binding.editProfileAge.setText(R.string.blank_age)
                    } else {
                        binding.editProfileAge.setText(viewModel.age)
                    }
                }
                val gender = snapshot.child("gender").getValue(String::class.java)
                gender?.let {
                    viewModel.gender = it
                    if (it == "") {
                        binding.editProfileGender.setText(R.string.error_invalid_Gender)
                    } else {
                        binding.editProfileGender.setText(viewModel.gender)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
            }
        })

        viewModel.userid = userId
    }

    private fun setupButtons(uid :String) {

        binding.backButton.setOnClickListener {
            sendResultAndFinish()
        }
        // disable
        binding.approveBtn.setOnClickListener {
            if(!clicked) {
                binding.approveBtn.text = getString(R.string.enable_acc)
                clicked = true
                UMViewModel.updateAccountStatus(uid, false)
            }else{
                binding.approveBtn.text = getString(R.string.disable_acc)
                clicked = false
                UMViewModel.updateAccountStatus(uid, true)
            }

        }
        //add cash
        binding.rejectBtn.setOnClickListener {
            val dialog= Dialog(binding.root.context)
            dialog.setContentView(R.layout.dialog_wallet_data)
            val today = GetData.getCurrentDateTime()

            val amountEditTxt = dialog.findViewById<TextInputEditText>(R.id.amount)
            val withdrawBtn = dialog.findViewById<Button>(R.id.withdraw_cash)
            val depositBtn = dialog.findViewById<Button>(R.id.deposit_cash)
            val deleteButton = dialog.findViewById<Button>(R.id.delete_card_button)
            val addCashToCardBtn = dialog.findViewById<Button>(R.id.add_cash_to_card)
            val cancelButton = dialog.findViewById<Button>(R.id.button_cancel)
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("VND")

            withdrawBtn.visibility = View.GONE
            depositBtn.visibility = View.GONE
            addCashToCardBtn.visibility = View.GONE
            deleteButton.text = getString(R.string.add_cash_to_wallet_btn)

            // add cash btn
            deleteButton.setOnClickListener {
                val amountTxt = amountEditTxt.text.toString().trim()
                val isValidAmountTxt = VerifyField.isValidMinCash(amountTxt)
                amountEditTxt.error = if (isValidAmountTxt) null else getString(R.string.no_amount)

                if (isValidAmountTxt) {
                    walletViewModel.addWalletAmount(uid,amountTxt.toFloat())

                    // noti
                    val notiRef = FirebaseDatabase.getInstance().getReference("Notifications").child(uid)
                    val notiId = notiRef.push().key.toString()
                    val amountConvertVnd = format.format(amountTxt.toDouble())
                    val newNoti = NotificationsRowModel(notiId, "Admin",
                        "+$amountConvertVnd ${getString(R.string.to_wallet2)}",
                        today)
                    notiRef
                        .child(notiId)
                        .setValue(newNoti)
                    amountEditTxt.setText("")
                    amountEditTxt.clearFocus()
                    Toast.makeText(applicationContext, getString(R.string.deposit_success), Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss() // Đóng dialog
            }

            dialog.setOnDismissListener {
                amountVM.fetchWalletAmount(uid)
                amountVM.walletAmount.observe(this){ amount->
                    binding.amountInWalletAmount.text =amount
                }
            }

            dialog.show()
        }
    }

    private fun sendResultAndFinish() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}