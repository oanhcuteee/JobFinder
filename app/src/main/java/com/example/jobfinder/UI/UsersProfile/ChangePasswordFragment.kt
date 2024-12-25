package com.example.jobfinder.UI.UsersProfile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.jobfinder.R
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backbtn.setOnClickListener{
            requireActivity().onBackPressed()
        }

        binding.btnSave.setOnClickListener{
            val newPassword = binding.newPass.text.toString()
            val ConfNewPassword = binding.confirmNewPass.text.toString()

            val isvalid_new_pass = VerifyField.isValidPassword(newPassword)
            val isvalid_renew_pass = if(newPassword != ConfNewPassword) false else true

            binding.newPass.error = if (isvalid_new_pass) null else getString(R.string.Error_invalid_newpass)
            binding.confirmNewPass.error = if (isvalid_renew_pass) null else getString(R.string.Error_invalid_confirm_newpass)

            if( isvalid_new_pass && isvalid_renew_pass){
                val userId = auth.currentUser
                userId?.let { currentUser ->
                    currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(requireContext(),R.string.Change_newpass_success,Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent()
                            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(),R.string.Change_newpass_failed,Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } else {
                checkToAutoFocus(isvalid_new_pass , isvalid_renew_pass)
            }

        }

    }

    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<EditText>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.newPass)
                    1 -> invalidFields.add(binding.confirmNewPass)
                }
            }
        }

        if (invalidFields.isNotEmpty()) {
            invalidFields.first().requestFocus()
        }
    }


}