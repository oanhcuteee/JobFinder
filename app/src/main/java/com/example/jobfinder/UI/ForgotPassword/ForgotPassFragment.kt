package com.example.jobfinder.UI.ForgotPassword

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jobfinder.Datas.Model.AppliedJobModel
import com.example.jobfinder.R
import com.example.jobfinder.Utils.PreventDoubleClick
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentForgotPassBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore


class ForgotPassFragment : Fragment() {
    private lateinit var binding: FragmentForgotPassBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentForgotPassBinding.inflate(inflater, container, false)
        
        // nút reset password
        binding.btnReset.setOnClickListener{
            if (PreventDoubleClick.checkClick()) {
                val emailInput = binding.emailInput.text.toString().trim()
                val isValidEmail = VerifyField.isValidEmail(emailInput) && emailInput.isNotEmpty()

                if (isValidEmail) {
                    binding.emailInput.error = null
                    binding.animationView.visibility = View.VISIBLE
                    sendEmailResetPass(emailInput)
                } else {
                    binding.emailInput.error = getString(R.string.error_invalid_email)
                    // Trỏ focus vào trường luôn
                    binding.emailInput.requestFocus()
                }
            }
        }

        return binding.root
    }


    private fun sendEmailResetPass(email: String) {
        auth = FirebaseAuth.getInstance()

        FirebaseDatabase.getInstance().getReference("UserBasicInfo").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var emailExists = false
                for (uid in task.result!!.children) {
                    val existEmail = uid.child("email").getValue(String::class.java)
                    if (existEmail == email) {
                        emailExists = true
                        break
                    }
                }
                if (emailExists) {
                    // Email tồn tại, tiến hành gửi yêu cầu đặt lại mật khẩu
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { resetTask ->
                            binding.animationView.visibility = View.GONE
                            if (resetTask.isSuccessful) {
                                updateUIForSuccessfulReset(email)
                            } else {
                                val exception = resetTask.exception
                                if (exception != null) {
                                    Toast.makeText(requireContext(), getString(R.string.FP_error_unknown), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                } else {
                    // Email không tồn tại
                    binding.animationView.visibility = View.GONE
                    resetUIToDefault()
                }
            } else {
                // Xử lý lỗi khi truy vấn dữ liệu
                binding.animationView.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.FP_error_unknown), Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun updateUIForSuccessfulReset(email: String) {
        binding.forgotTitle.setText(R.string.FP_Title2)
        binding.subTitle1.setText(R.string.FP_subTitle2)
        binding.image.setImageResource(R.drawable.ic_checkemail)
        binding.subTitle2.setText(email)
        Toast.makeText(requireContext(), getString(R.string.FP_toast), Toast.LENGTH_SHORT).show()
    }

    private fun resetUIToDefault() {
        binding.forgotTitle.setText(R.string.forgot_pass)
        binding.subTitle1.setText(R.string.FP_subTitle1)
        binding.image.setImageResource(R.drawable.ic_forgotfrag)
        binding.subTitle2.setText(R.string.FP_subTitle1a)
        Toast.makeText(requireContext(), getString(R.string.FP_Emailnot_registered), Toast.LENGTH_SHORT).show()
    }


}