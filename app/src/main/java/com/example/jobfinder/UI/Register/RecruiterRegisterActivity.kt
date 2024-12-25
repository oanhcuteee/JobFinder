package com.example.jobfinder.UI.Register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.example.jobfinder.Datas.Model.BUserInfo
import com.example.jobfinder.R
import com.example.jobfinder.UI.Home.HomeActivity
import com.example.jobfinder.Utils.PreventDoubleClick
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.ActivityRecruiterRegisterBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.jobfinder.Datas.Model.UserBasicInfoModel
import com.example.jobfinder.Datas.Model.idAndRole
import com.example.jobfinder.Datas.Model.walletAmountModel
import com.example.jobfinder.UI.Admin.Statistical.AdminUserCountViewModel
import com.example.jobfinder.Utils.GetData


class RecruiterRegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRecruiterRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: AdminUserCountViewModel by viewModels()
//    private var isPassVisible = PasswordToggleState(false)
//    private var isCalendarVisible = CalendarToggleState(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecruiterRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

//        // chọn ngày thành lập
//        fragmentManager = supportFragmentManager
//        binding.foundationDay.setOnClickListener{
//            val title = getString(R.string.rec_foundationDay)
//            val showTextPlace = binding.foundationDay
//            Calendar.showDatePickerDialog(getActivityFragmentManager(), isCalendarVisible, title, showTextPlace)
//        }

        // Đăng ký
        binding.btnRegister.setOnClickListener{
            if (PreventDoubleClick.checkClick()) {
                val nameInput = binding.recName.text.toString()
                val hotlineInput = binding.recHotline.text.toString()
                val addressInput = binding.recAddress.text.toString()
                val emailInput = binding.recEmail.text.toString().trim()
                val passInput = binding.password.text.toString()
                val repassInput = binding.reEnterPass.text.toString()

                val isValidName = nameInput.isNotEmpty()
                val isValidHotline = VerifyField.isValidPhoneNumber(hotlineInput)
                val isValidAddress = addressInput.isNotEmpty()
                val isValidEmail = VerifyField.isValidEmail(emailInput)
                val isValidPassword = VerifyField.isValidPassword(passInput)
                val isValidRePassword = VerifyField.isValidPassword(repassInput) && repassInput == passInput

                binding.recName.error = if (isValidName) null else getString(R.string.error_invalid_name)
                binding.recHotline.error = if (isValidHotline) null else getString(R.string.error_invalid_hotline)
                binding.recAddress.error = if (isValidAddress) null else getString(R.string.error_invalid_addr)
                binding.recEmail.error = if (isValidEmail) null else getString(R.string.error_invalid_email)
                binding.password.error = if (isValidPassword) null else getString(R.string.error_pass)
                binding.reEnterPass.error = if (isValidRePassword) null else getString(R.string.error_invalid_reEnterPass)

                if (isValidName && isValidHotline && isValidAddress && isValidEmail && isValidPassword && isValidRePassword) {
                    auth.createUserWithEmailAndPassword(emailInput,passInput).addOnCompleteListener(this) { task->
                        if (task.isSuccessful) {
                            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                            val today = GetData.getCurrentDateTime()
                            val todayStr = GetData.getDateFromString(today)
                            val uid = auth.currentUser?.uid
                            val userBasicInfo = UserBasicInfoModel(uid, nameInput, emailInput, hotlineInput,addressInput)
                            val bUserInfo= BUserInfo(uid,"", "","","")
                            val userRole= idAndRole(uid, "BUser")
                            viewModel.pushRegisteredUserToFirebaseByDate("BUser", "1", todayStr)
                            val walletAmount = walletAmountModel("0.0")
                            FirebaseDatabase.getInstance()
                                .getReference("WalletAmount")
                                .child(uid.toString())
                                .setValue(walletAmount)
                            FirebaseDatabase.getInstance().getReference("UserRole").child(uid.toString()).setValue(userRole)
                            FirebaseDatabase.getInstance().getReference("UserBasicInfo").child(uid.toString()).setValue(userBasicInfo)
                            FirebaseDatabase.getInstance().getReference("BUserInfo").child(uid.toString()).setValue(bUserInfo)
                            startActivity(Intent(this, HomeActivity::class.java))
                            Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, getString(R.string.register_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    checkToAutoFocus(isValidName, isValidHotline, isValidAddress, isValidEmail, isValidPassword, isValidRePassword)
                }
            }
        }

        // từ recruiter trở về login
        binding.returnbackLogin.setOnClickListener{
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
                    0 -> invalidFields.add(binding.recName)
                    1 -> invalidFields.add(binding.recHotline)
                    2 -> invalidFields.add(binding.recAddress)
                    3 -> invalidFields.add(binding.recEmail)
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