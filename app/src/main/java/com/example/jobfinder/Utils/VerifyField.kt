package com.example.jobfinder.Utils
import android.text.method.PasswordTransformationMethod
import androidx.core.util.PatternsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


data class PasswordToggleState(var isPassVisible: Boolean)

object VerifyField {

    //thay đổi icon
    fun changeIconShowPassword(
        inputLayout: TextInputLayout,
        passwordToggleState: PasswordToggleState,
        inputEditText: TextInputEditText
    ) {
        inputLayout.setEndIconOnClickListener {
            passwordToggleState.isPassVisible = !passwordToggleState.isPassVisible
            togglePasswordVisible(inputEditText, passwordToggleState.isPassVisible)
        }
    }
    // ẩn hiện password
    fun togglePasswordVisible(editText: TextInputEditText, isPassVisible: Boolean) {
        if (isPassVisible) editText.transformationMethod = null // transformationMethod = null thì xem dc text
        else editText.transformationMethod = PasswordTransformationMethod.getInstance()
        editText.text?.let { editText.setSelection(it.length) } //di chuyển con trỏ về cuối text
    }

    fun isValidName(username: String): Boolean {
        return username.isNotEmpty() && username.length <= 50
    }

    // check email hợp lệ
    fun isValidEmail(email: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches() && email.isNotEmpty()
    }

    // check độ dài số điện thoại
     fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotEmpty() && phoneNumber.length == 10
    }
    fun isValidAge(Age: String): Boolean {
        val age = Age.toIntOrNull()
        return age != null && age in 18 until 81 && Age.matches(Regex("\\d+"))
    }
    fun isValidTaxCode(Tax: String): Boolean {
        return Tax.isNotEmpty() && (Tax.length == 10 || Tax.length == 13)
    }

    // check pass hợp lệ
    fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty() && password.length >= 6
    }

    fun isValidCardNumber(cardNum: String): Boolean{
        return cardNum.isNotEmpty() && cardNum.length >=10 &&cardNum.length<= 16
    }

    fun isEmpty(string: String): Boolean{
        return  string.isNotEmpty() && string.length >=1
    }

    fun maxEmpAmount(empAmount : String): Boolean{
        if( empAmount == ""){
            return false
        }
        return empAmount.toInt() <=10
    }

    fun isValidMinCash(amount:String):Boolean{
        if(amount ==""){
            return false
        }
        return amount.toFloat() > 0
    }

    fun duplicatePhoneNum(phoneNum: String, callback: (Boolean) -> Unit) {
        FirebaseDatabase.getInstance().getReference("UserBasicInfo").get().addOnSuccessListener { dataSnapshot ->
            var isDuplicate = false
            for (user in dataSnapshot.children) {
                val userPhoneNum = user.child("phone_num").getValue(String::class.java)
                if (phoneNum == userPhoneNum) {
                    isDuplicate = true
                    break
                }
            }
            callback(!isDuplicate)
        }.addOnFailureListener {
            // Handle any potential errors here
            callback(false) // Assuming that if there's a failure, we don't want to allow duplicates
        }
    }

}