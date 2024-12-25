package com.example.jobfinder.UI.UserDetailInfo

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
import com.example.jobfinder.UI.Admin.UserManagement.AdminUserManagementViewModel
import com.example.jobfinder.UI.PostedJob.PostedJobViewModel
import com.example.jobfinder.UI.UsersProfile.ProfileViewModel
import com.example.jobfinder.UI.Wallet.WalletCardListViewModel
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.ActivityBuserDetailInfoBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Currency


class BUserDetailInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuserDetailInfoBinding
    private val  viewModel: ProfileViewModel by viewModels()
    private val walletViewModel : PostedJobViewModel by viewModels()
    private val amountVM: WalletCardListViewModel by viewModels()
    private val UMViewModel: AdminUserManagementViewModel by viewModels()
    private var clicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuserDetailInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = FirebaseDatabase.getInstance().reference

        val userId = intent.getStringExtra("uid")
        val accStatus = intent.getStringExtra("accStatus")

        setUpBtn(userId.toString())

        if (accStatus != null){
            binding.recuitterInfoBtnHolder.visibility = View.VISIBLE
            binding.amountWrapper.visibility = View.VISIBLE
            amountVM.fetchWalletAmount(userId.toString())

            amountVM.walletAmount.observe(this){ amount->
                binding.amountInWalletAmount.text =amount
            }
            if (accStatus == "active"){
                binding.disableBtn.text = getString(R.string.disable_acc)
                clicked = false
            }else{
                binding.disableBtn.text = getString(R.string.enable_acc)
                clicked = true
            }
        }
        userId?.let {

            database.child("UserBasicInfo").child(it).addListenerForSingleValueEvent(object :
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

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RecruterEditProfileFragment", "Database error: ${error.message}")
                }
            })
            database.child("BUserInfo").child(it).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val description = snapshot.child("description").getValue(String::class.java)
                    description?.let {
                        viewModel.des = it
                        if(it == ""){
                            binding.editProfileDescription.setText(R.string.no_job_des2)
                        }else {
                            binding.editProfileDescription.setText(viewModel.des)
                        }
                    }
                    val busType = snapshot.child("business_type").getValue(String::class.java)
                    busType?.let {
                        viewModel.busType = it
                        if(it == ""){
                            binding.editProfileBustype.setText(R.string.error_invalid_BusSec)
                        }else {
                            binding.editProfileBustype.setText(viewModel.busType)
                        }
                    }
                    val busSec = snapshot.child("business_sector").getValue(String::class.java)
                    busSec?.let {
                        viewModel.busSec = it
                        if(it == ""){
                            binding.editProfileBusSec.text = getString(R.string.blank_sector)
                        }else {
                            binding.editProfileBusSec.text = viewModel.busSec
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RecruterEditProfileFragment", "Database error: ${error.message}")
                }
            })

            viewModel.userid = it
            binding.animationView.visibility = View.GONE

        }

        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        RetriveImg.retrieveImage(viewModel.userid, binding.profileImage)
    }

    private fun setUpBtn(uid:String){
        binding.addCashBtn.setOnClickListener {
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
                    walletViewModel.addWalletAmount(uid,amountTxt.toFloat() )
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

        binding.disableBtn.setOnClickListener {
            if(!clicked) {
                binding.disableBtn.text = getString(R.string.enable_acc)
                clicked = true
                UMViewModel.updateAccountStatus(uid, false)
            }else{
                binding.disableBtn.text = getString(R.string.disable_acc)
                clicked = false
                UMViewModel.updateAccountStatus(uid, true)
            }
        }
    }
}