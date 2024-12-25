package com.example.jobfinder.UI.UsersProfile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.jobfinder.R
import com.example.jobfinder.UI.AboutUs.AboutUsActivity
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentSettingsMenuBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class SettingsMenuFragment : Fragment() {
    private lateinit var binding: FragmentSettingsMenuBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: SettingsMenuViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsMenuBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check bật tắt sinh trắc bằng email
        val currentUser = auth.currentUser
        currentUser?.email?.let { email ->
            Log.d("curr_email_fetchFromFirebase", email)
            viewModel.getUserByEmail(email) { user ->
                user?.let {
                    binding.biometricSwitch.isChecked = it.isBiometricEnabled
                    Log.d("biometricSwitch", it.isBiometricEnabled.toString())
                } ?: run {
                    Log.d("getUserByEmail", "User not found")
                }
            }
        }

        // Cập nhật trạng thái bật tắt sinh trắc nếu có thay đổi vào db
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("biometricSwitch", "Checked change detected")
            currentUser?.email?.let { email ->
                viewModel.getUserByEmail(email) { user ->
                    user?.let {
                        it.isBiometricEnabled = isChecked
                        viewModel.updateUser(it)
                        Log.d("isChecked", isChecked.toString())
                    } ?: run {
                        Log.d("getUserByEmail", "User not found for update")
                    }
                }
            }
        }

        binding.changePassword.setOnClickListener(){
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.password_dialog, null)
            val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
            val title = dialogView.findViewById<TextView>(R.id.dialog_title)
            val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancle)
            val confirmButton = dialogView.findViewById<Button>(R.id.btn_confirm)
            val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.current_pass)
            val forgotPassword = dialogView.findViewById<TextView>(R.id.forgot_pass)

            title.setText(getString(R.string.Confirm_change_pass))

            val alertDialog = builder.create()
            alertDialog.show()
            cancelButton.setOnClickListener {
                alertDialog.dismiss()
            }
            confirmButton.setOnClickListener {
                val currentPassword = currentPasswordEditText.text.toString()
                val isvalid_pass = VerifyField.isValidPassword(currentPassword)
                currentPasswordEditText.error = if (isvalid_pass) null else getString(R.string.error_pass)

                if (isvalid_pass){
                    val userId = auth.currentUser
                    userId?.let { currentUser ->
                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                        currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                // Cập nhật trạng thái sinh trắc về false
                                viewModel.getUserByEmail(currentUser.email!!) { user ->
                                    user?.let {
                                        it.isBiometricEnabled = false
                                        viewModel.updateUser(it)
                                    }
                                }
                                val activity = requireActivity() as SettingsActivity
                                activity.replaceFragment(ChangePasswordFragment())
                                alertDialog.dismiss()
                            }else{
                                Toast.makeText(
                                    requireContext(),
                                    R.string.re_authen_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            forgotPassword.setOnClickListener(){
                val intent = Intent(requireContext(), ProfileForgotPasswordActivity::class.java)
                startActivity(intent)
                alertDialog.dismiss()
            }
        }

//        binding.deleteAccount.setOnClickListener(){
//            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.password_dialog, null)
//            val builder = AlertDialog.Builder(requireContext()).setView(dialogView)
//            val title = dialogView.findViewById<TextView>(R.id.dialog_title)
//            val cancelButton = dialogView.findViewById<Button>(R.id.btn_cancle)
//            val confirmButton = dialogView.findViewById<Button>(R.id.btn_confirm)
//            val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.current_pass)
//            val forgotPassword = dialogView.findViewById<TextView>(R.id.forgot_pass)
//            title.setText(getString(R.string.Confirm_delete_account))
//
//            val alertDialog = builder.create()
//            alertDialog.show()
//            cancelButton.setOnClickListener {
//                alertDialog.dismiss()
//            }
//            confirmButton.setOnClickListener {
//                val currentPassword = currentPasswordEditText.text.toString()
//                val isvalid_pass = VerifyField.isValidPassword(currentPassword)
//                currentPasswordEditText.error = if (isvalid_pass) null else getString(R.string.error_pass)
//
//                if (isvalid_pass){
//                    val userId = auth.currentUser
//                    userId?.let { currentUser ->
//                        val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
//                        currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
//                            if (reauthTask.isSuccessful) {
//                                val uid = auth.currentUser?.uid.toString()
//                                currentUser.delete().addOnCompleteListener { deleteTask ->
//                                    if (deleteTask.isSuccessful) {
//                                        deleteImage(uid)
//                                        deleteUserData(uid)
//                                        Toast.makeText(requireContext(), getString(R.string.account_deleted), Toast.LENGTH_SHORT ).show()
//                                        alertDialog.dismiss()
//                                        val intent = Intent(requireContext(), SelectRoleActivity::class.java)
//                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                        startActivity(intent)
//                                        requireActivity().finishAffinity()
//                                    } else {
//                                        Toast.makeText(requireContext(),getString(R.string.delete_account_failed),Toast.LENGTH_SHORT ).show()
//                                    }
//                                }
//                            }else{
//                                Toast.makeText(requireContext(),R.string.re_authen_failed,Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
//            }
//            forgotPassword.setOnClickListener(){
//                val intent = Intent(requireContext(), ProfileForgotPasswordActivity::class.java)
//                startActivity(intent)
//                alertDialog.dismiss()
//            }
//        }

        binding.aboutApp.setOnClickListener {
            val intent = Intent(requireContext(),AboutUsActivity::class.java)
            startActivity(intent)
        }

        binding.backbtn.setOnClickListener(){
            val resultIntent = Intent()
            requireActivity().setResult(Activity.RESULT_OK, resultIntent)
            requireActivity().finish()
        }

    }

    fun deleteUserData(uid: String) {
        val database = FirebaseDatabase.getInstance()
        val reference = database.reference

        val tableRefs = listOf(
            "Applicant","Appliedjob", "Approvedjob", "BUserInfo", "Job", "NUserInfo", "Notifications",
            "Support", "UserBasicInfo", "UserRole", "Wallet",
            "WalletAmount", "WalletHistory"
        )

        // Lặp qua từng bảng dữ liệu trong tableRefs để xóa dữ liệu của uid
        for (table in tableRefs) {
            val tableReference = reference.child(table).child(uid)
            tableReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        tableReference.removeValue()
                            .addOnSuccessListener {
                                println("Đã xóa dữ liệu của $uid từ bảng $table thành công")
                            }
                            .addOnFailureListener { e ->
                                println("Lỗi khi xóa dữ liệu của $uid từ bảng $table: ${e.message}")
                            }
                    } else {
                        println("Không có dữ liệu để xóa cho $uid từ bảng $table")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Đã xảy ra lỗi: ${error.message}")
                }
            })
        }

    }

    private fun deleteImage(uid: String) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
        val imageRef: StorageReference = storageReference.child(uid)
        imageRef.delete().addOnSuccessListener {

            println("Đã xóa dữ liệu của $uid trong Firebase Storage thành công")

        }.addOnFailureListener {e ->
            println("Lỗi khi xóa dữ liệu của $uid trong Firebase Storage: ${e.message}")
        }

    }









}