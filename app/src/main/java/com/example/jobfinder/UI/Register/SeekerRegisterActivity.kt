package com.example.jobfinder.UI.Register

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.jobfinder.Datas.Model.NUserInfo
import com.example.jobfinder.Datas.Model.UserBasicInfoModel
import com.example.jobfinder.Datas.Model.idAndRole
import com.example.jobfinder.Datas.Model.walletAmountModel
import com.example.jobfinder.R
import com.example.jobfinder.UI.Admin.Statistical.AdminUserCountViewModel
import com.example.jobfinder.UI.Home.HomeActivity
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.Utils.PreventDoubleClick
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.ActivitySeekerRegisterBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SeekerRegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivitySeekerRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: AdminUserCountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeekerRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // Đăng ký
        binding.btnRegister.setOnClickListener{
            if (PreventDoubleClick.checkClick()) {
                val nameInput = binding.seekName.text.toString()
                val phoneInput = binding.seekPhonenums.text.toString()
                val addressInput = binding.seekAddress.text.toString()
                val emailInput = binding.seekEmail.text.toString().trim()
                val passInput = binding.password.text.toString()
                val repassInput = binding.reEnterPass.text.toString()

                val isValidName = nameInput.isNotEmpty()
                val isValidPhone = VerifyField.isValidPhoneNumber(phoneInput)
                val isValidAddress = addressInput.isNotEmpty()
                val isValidEmail = VerifyField.isValidEmail(emailInput)
                val isValidPassword = VerifyField.isValidPassword(passInput)
                val isValidRePassword = VerifyField.isValidPassword(repassInput) && repassInput == passInput

                binding.seekName.error = if (isValidName) null else getString(R.string.error_invalid_name)
                binding.seekPhonenums.error = if (isValidPhone) null else getString(R.string.error_invalid_phone)
                binding.seekAddress.error = if (isValidAddress) null else getString(R.string.error_invalid_addr)
                binding.seekEmail.error = if (isValidEmail) null else getString(R.string.error_invalid_email)
                binding.password.error = if (isValidPassword) null else getString(R.string.error_pass)
                binding.reEnterPass.error = if (isValidRePassword) null else getString(R.string.error_invalid_reEnterPass)

                if (isValidName && isValidPhone && isValidAddress && isValidEmail && isValidPassword && isValidRePassword) {
                    auth.createUserWithEmailAndPassword(emailInput,passInput).addOnCompleteListener(this) { task->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                            val today = GetData.getCurrentDateTime()
                            val todayStr = GetData.getDateFromString(today)
                            val uid = auth.currentUser?.uid
                            val userBasicInfo = UserBasicInfoModel(uid, nameInput, emailInput, phoneInput,addressInput)
                            val nUserInfo = NUserInfo("","")
                            val userRole = idAndRole(uid, "NUser")
                            val walletAmount = walletAmountModel("0.0")
                            FirebaseDatabase.getInstance()
                                .getReference("WalletAmount")
                                .child(uid.toString())
                                .setValue(walletAmount)
                            FirebaseDatabase.getInstance().getReference("UserRole").child(uid.toString()).setValue(userRole)
                            FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(uid.toString()).setValue(userBasicInfo)
                            FirebaseDatabase.getInstance().getReference("NUserInfo").child(uid.toString()).setValue(nUserInfo)
                            viewModel.pushRegisteredUserToFirebaseByDate("NUser", "1", todayStr)
                            startActivity(Intent(this, HomeActivity::class.java))
                            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    checkToAutoFocus(isValidName, isValidPhone, isValidAddress, isValidEmail, isValidPassword, isValidRePassword)
                }
            }
        }

        // trở về login
        binding.returnbackLogin.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    }

    // Check các trường sai và auto focus vào trường đầu tiên bị sai
    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<TextInputEditText>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.seekName)
                    1 -> invalidFields.add(binding.seekPhonenums)
                    2 -> invalidFields.add(binding.seekAddress)
                    3 -> invalidFields.add(binding.seekEmail)
                    4 -> invalidFields.add(binding.password)
                    5 -> invalidFields.add(binding.reEnterPass)
                }
            }
        }

        if (invalidFields.isNotEmpty()) {
            invalidFields.first().requestFocus()
        }
    }
}