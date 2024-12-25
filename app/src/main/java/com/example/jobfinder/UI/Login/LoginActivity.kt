package com.example.jobfinder.UI.Login

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.jobfinder.Datas.Model.idAndRole
import com.example.jobfinder.R
import com.example.jobfinder.UI.Admin.Home.AdminHomeActivity
import com.example.jobfinder.UI.ForgotPassword.ForgotPassActivity
import com.example.jobfinder.UI.Home.HomeActivity
import com.example.jobfinder.UI.Register.RecruiterRegisterActivity
import com.example.jobfinder.UI.Register.SeekerRegisterActivity
import com.example.jobfinder.UI.UsersProfile.SettingsMenuViewModel
import com.example.jobfinder.Utils.PasswordToggleState
import com.example.jobfinder.Utils.PreventDoubleClick
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private var isPassVisible = PasswordToggleState(false)
    private lateinit var auth: FirebaseAuth
    private val settingsMenuVM: SettingsMenuViewModel by viewModels()
    private val LOGIN_REQUEST_CODE = 100

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var userType: String
    private var isEmailChanged = false

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Khởi tạo SharedPreferences và Editor
        sharedPreferences = getSharedPreferences("email_preferences", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Lấy email cuối cùng đăng nhập
        val savedEmail = sharedPreferences.getString("last_login_email", "").toString()
        val savedPass = sharedPreferences.getString("last_login_password", "").toString()
        binding.userEmailLogin.setText(savedEmail)


        // Biometric Authentication
        setupBiometricPrompt(savedEmail, savedPass)

        // gọi hàm đổi icon và ẩn hiện password
        VerifyField.changeIconShowPassword(binding.passwordTextInputLayout, isPassVisible, binding.userPassLogin)

        // Lấy role từ bên select role
        userType = intent.getStringExtra("user_type").toString()

        // Mở register
        binding.openRegisterActi.setOnClickListener {
            if (PreventDoubleClick.checkClick()) {
                val intent = if (userType == "NUser") {
                    Intent(this, SeekerRegisterActivity::class.java)
                } else {
                    Intent(this, RecruiterRegisterActivity::class.java)
                }
                startActivity(intent)
            }
        }

        // Hiển thị tiêu đề dựa vào role đã chọn
        if (userType == "NUser") {
            binding.titleLogin1.setText(R.string.welcome_seek1)
            binding.titleLogin2.setText(R.string.welcome_seek2)
        } else {
            binding.titleLogin1.setText(R.string.welcome_rec1)
            binding.titleLogin2.setText(R.string.welcome_rec2)
        }

        // Xác nhận để Login
        binding.btnLogin.setOnClickListener {
            // chạy animation loading
            binding.animationView.visibility = View.VISIBLE

            val emailInput = binding.userEmailLogin.text.toString().trim()
            val passInput = binding.userPassLogin.text.toString()
            val isEmailValid = emailInput.isNotEmpty() && VerifyField.isValidEmail(emailInput)
            val isPassValid = VerifyField.isValidPassword(passInput)

            binding.userEmailLogin.error = if (isEmailValid) null else {
                binding.animationView.visibility = View.GONE
                getString(R.string.error_invalid_email)
            }

            binding.userPassLogin.error = if (isPassValid) null else {
                binding.animationView.visibility = View.GONE
                getString(R.string.error_pass)
            }

            if (isEmailValid && isPassValid) {
                auth.signInWithEmailAndPassword(emailInput, passInput).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            FirebaseDatabase.getInstance().getReference("UserRole").child(uid).get()
                                .addOnSuccessListener { snapshot ->
                                    val data: idAndRole? = snapshot.getValue(idAndRole::class.java)
                                    if (data != null) {
                                        // Lưu email lần cuối đăng nhập trước khi đăng xuất vào SharedPreferences
                                        editor.putString("last_login_email", emailInput)
                                        editor.putString("last_login_password", passInput)
                                        editor.apply()

                                        if(data.accountStatus == "active") {
                                            checkRole(data.role.toString(), userType)
                                        }else{
                                            Toast.makeText(applicationContext, getString(R.string.disabled_account), Toast.LENGTH_SHORT).show()
                                        }

                                        // thêm email và pass vào room db nếu chưa có
                                        settingsMenuVM.getUserByEmail(emailInput) { user ->
                                            if (user == null) {
                                                val newUser = UsersDataSavedModel(emailInput, passInput)
                                                settingsMenuVM.insertUser(newUser)
                                            }
                                        }
                                    } else {
                                        Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                    }
                                    binding.animationView.visibility = View.GONE
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Login button", "Something wrong while getting data", e)
                                    Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                    binding.animationView.visibility = View.GONE
                                }
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                            binding.animationView.visibility = View.GONE
                        }
                    } else {
                        binding.animationView.visibility = View.GONE
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(applicationContext, getString(R.string.error_wrong_passwordOrUsername), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                        }
                        checkToAutoFocus(isEmailValid, isPassValid)
                    }
                }
            } else {
                checkToAutoFocus(isEmailValid, isPassValid)
            }
        }

        // Quên mật khẩu
        binding.moveToForgotBtn.setOnClickListener {
            if (PreventDoubleClick.checkClick()) {
                val intent = Intent(this, ForgotPassActivity::class.java)
                startActivity(intent)
            }
        }

        // Theo dõi thay đổi email input để bật tắt sinh trắc bên dưới
        binding.userEmailLogin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentEmail = s.toString().trim()
                val isEmailValid = currentEmail.isNotEmpty() && VerifyField.isValidEmail(currentEmail)
                isEmailChanged = isEmailValid && currentEmail != savedEmail
            }
        })

        // Đăng nhập vân tay
        binding.btnFingerprintLogin.setOnClickListener {
            val db = RoomDB.getDatabase(applicationContext)
            val currentEmail = binding.userEmailLogin.text.toString().trim()
            lifecycleScope.launch {
                val user = db.usersDao().getUserByEmail(currentEmail)
                if (isEmailChanged || currentEmail != savedEmail || user == null || !user.isBiometricEnabled) {
                    val biometricActivationDialog = NotifyBiometricDialog(this@LoginActivity)
                    biometricActivationDialog.show()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {   // Kiểm tra phiên bản SDK
                        setupBiometricPrompt(savedEmail, savedPass)
                        biometricPrompt.authenticate(promptInfo)    // Hiện hộp xác thực
                    }
                }
            }
        }


    }

    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<TextInputEditText>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.userEmailLogin)
                    1 -> invalidFields.add(binding.userPassLogin)
                }
            }
        }
        if (invalidFields.isNotEmpty()) {
            invalidFields.first().requestFocus()
        }
    }

    private fun checkRole(role: String, userType: String) {
        if (role == userType && role != "Admin") {
            navigateToHome()
        } else if (role == "Admin") {
            navigateToAdminHome()
        } else {
            Toast.makeText(applicationContext, getString(R.string.wrong_role), Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToAdminHome() {
        val intent = Intent(this, AdminHomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupBiometricPrompt(email:String, pass:String) {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, errString, Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                FirebaseDatabase.getInstance().getReference("UserRole").child(uid)
                                    .get()
                                    .addOnSuccessListener { snapshot ->
                                        val data: idAndRole? = snapshot.getValue(idAndRole::class.java)
                                        if (data != null) {
                                            if(data.accountStatus == "active") {
                                                checkRole(data.role.toString(), userType)
                                            }else{
                                                Toast.makeText(applicationContext, getString(R.string.disabled_account), Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                        }
                                        binding.animationView.visibility = View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                        binding.animationView.visibility = View.GONE
                                    }
                            } else {
                                Toast.makeText(applicationContext, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                                binding.animationView.visibility = View.GONE
                            }
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, getString(R.string.fingerAuth_failed), Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(R.string.fingerAuth_title))
            .setNegativeButtonText(getString(R.string.fingerAuth_usePassword))
            .build()
    }


    // Xử lý kết quả từ Homeactivity
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Đăng nhập thành công, kết thúc cả SelectRoleActivity và LoginActivity
            finish()
        }
    }

}