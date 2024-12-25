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
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.jobfinder.R
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentSeekerEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SeekerEditProfileFragment : Fragment() {
    private lateinit var binding: FragmentSeekerEditProfileBinding
    private lateinit var auth: FirebaseAuth
    lateinit var viewModel: ProfileViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

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
                    Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
                }
            })
            database.child("NUserInfo").child(it).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val age = snapshot.child("age").getValue(String::class.java)
                    age?.let {
                        viewModel.age = it
                        if (it == "") {
                            binding.editProfileAge.setHint(R.string.blank_age)
                        } else {
                            binding.editProfileAge.setText(viewModel.age)
                        }
                    }
                    val gender = snapshot.child("gender").getValue(String::class.java)
                    gender?.let {
                        viewModel.gender = it
                        if (it == "") {
                            binding.editProfileGender.setHint(R.string.error_invalid_Gender)
                        } else {
                            binding.editProfileGender.setText(viewModel.gender)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("SeekerEditProfileFragment", "Database error: ${error.message}")
                }
            })
            viewModel.userid = it

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSeekerEditProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    // fetch ảnh
    override fun onResume() {
        super.onResume()
        retrieveImage(viewModel.userid)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = binding.editProfileName
        val email = binding.editProfileEmail
        val address = binding.editProfileAddress
        val phone = binding.editProfilePhonenum
        val age = binding.editProfileAge
        val gender = binding.editProfileGender
        val save = binding.editProfileSaveChange
        val image = binding.uploadImage
        var isEdited = false
        disableButton(isEdited)

        //button sửa
        binding.editProfileEditbtn.setOnClickListener {
            isEdited =!isEdited
            if (isEdited == false){
                if( checkIfEdited() == true) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.profile_cancle_edit_confirm)
                    builder.setMessage(R.string.profile_cancle_edit_confirm_noti)
                    builder.setPositiveButton(R.string.profile_image_delete_confim_yes) { dialog, which ->
                        binding.editProfileName.setText(viewModel.name)
                        binding.editProfileEmail.setText(viewModel.email)
                        binding.editProfilePhonenum.setText(viewModel.phone)
                        binding.editProfileAddress.setText(viewModel.address)
                        binding.editProfileAge.setText(viewModel.age)
                        binding.editProfileGender.setText(viewModel.gender)
                        binding.account.setText(getString(R.string.profile))
                        Toast.makeText(requireContext(), getString(R.string.edit_profile_disable), Toast.LENGTH_SHORT).show()
                        disableButton(isEdited)
                    }
                    builder.setNegativeButton(R.string.profile_image_delete_confim_no) { dialog, which ->
                        dialog.dismiss()
                        isEdited = true
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
                else{
                    disableButton(isEdited)
                    binding.account.setText(getString(R.string.profile))
                    Toast.makeText(requireContext(), getString(R.string.edit_profile_disable), Toast.LENGTH_SHORT).show()
                }

                //bật edit
            } else if (isEdited == true){
                disableButton(isEdited)
                binding.account.setText(getString(R.string.edit_profile))
                Toast.makeText(requireContext(), getString(R.string.edit_profile_enable), Toast.LENGTH_SHORT).show()
            }
        }

        //button save
        save.setOnClickListener {
            val newName = name.text.toString()
            val newAddress = address.text.toString()
            val newPhone = phone.text.toString()
            val newAge = age.text.toString()
            val newGender = gender.text.toString()

            val isValidName = VerifyField.isValidName(newName)
            val isValidAddress = newAddress.isNotEmpty()
            val isValidPhone = VerifyField.isValidPhoneNumber(newPhone)
            val isValidAge = VerifyField.isValidAge(newAge)
            val isValidGender = newGender.isNotEmpty()


            name.error = if (isValidName) null else getString(R.string.error_invalid_name)
            address.error = if (isValidAddress) null else getString(R.string.error_invalid_addr)
            phone.error = if (isValidPhone) null else getString(R.string.error_invalid_hotline)
            age.error = if (isValidAge) null else getString(R.string.error_invalid_Age)
            gender.error = if (isValidGender) null else getString(R.string.error_invalid_Gender)

            if( checkIfEdited() == true) {
                if (isValidName && isValidAddress && isValidPhone && isValidAge) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    userId?.let {
                        viewModel.name = newName
                        viewModel.address = newAddress
                        viewModel.phone = newPhone
                        viewModel.age = newAge
                        viewModel.gender = newGender

                        val userBI =
                            FirebaseDatabase.getInstance().reference.child("UserBasicInfo").child(it)
                        val NUser =
                            FirebaseDatabase.getInstance().reference.child("NUserInfo").child(it)
                        userBI.child("name").setValue(viewModel.name)
                        userBI.child("address").setValue(viewModel.address)
                        userBI.child("phone_num").setValue(viewModel.phone)
                        NUser.child("age").setValue(viewModel.age)
                        NUser.child("gender").setValue(viewModel.gender)

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.profile_edited),
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                    isEdited = false
                    disableButton(isEdited)
                } else {
                    checkToAutoFocus(isValidName, isValidAddress, isValidPhone, isValidAge)
                }
            }else{
                isEdited = false
                disableButton(isEdited)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.edit_profile_nothing_change),
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        //button back
        binding.editProfileBackbtn.setOnClickListener {
            if( checkIfEdited() == true && isEdited == true) {
                val newAge = age.text.toString()
                val newGender = gender.text.toString()
                val isValidAge = VerifyField.isValidAge(newAge)
                val isValidGender = newGender.isNotEmpty()
                age.error = if (isValidAge) null else getString(R.string.error_invalid_Age)
                gender.error = if (isValidGender) null else getString(R.string.error_invalid_Gender)

                if (isValidAge && isValidGender) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.profile_cancle_edit_confirm)
                    builder.setMessage(R.string.profile_cancle_edit_confirm_noti)
                    builder.setPositiveButton(R.string.profile_image_delete_confim_yes) { dialog, which ->
                        val resultIntent = Intent()
                        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                        requireActivity().finish()
                    }
                    builder.setNegativeButton(R.string.profile_image_delete_confim_no) { dialog, which ->
                        dialog.dismiss()

                    }
                    val dialog = builder.create()
                    dialog.show()
                }else {
                    println("error")
                }

            }
            else{
                val resultIntent = Intent()
                requireActivity().setResult(Activity.RESULT_OK, resultIntent)
                requireActivity().finish()
            }


        }

        //button upload image
        binding.uploadImage.setOnClickListener() {
            val intent = Intent(requireContext(), profile_upload_image::class.java)
            startActivity(intent)
        }

        gender.setOnClickListener(){
            GenderSelect(it)

        }


    }

    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<EditText>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.editProfileName)
                    1 -> invalidFields.add(binding.editProfilePhonenum)
                    2 -> invalidFields.add(binding.editProfileAddress)
                    3 -> invalidFields.add(binding.editProfileAge)

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

    private fun GenderSelect(view: View){
        showPopupMenu(view, R.menu.gender_menu) {
            binding.editProfileGender.text = it
        }
    }

    private fun disableButton(isEdited : Boolean) {
        binding.editProfileName.isEnabled = isEdited
        binding.editProfileAddress.isEnabled = isEdited
        binding.editProfilePhonenum.isEnabled = isEdited
        binding.editProfileAge.isEnabled = isEdited
        binding.editProfileGender.isEnabled = isEdited
        binding.uploadImage.visibility = if (isEdited) View.VISIBLE else View.GONE
        binding.editProfileSaveChange.visibility = if (isEdited) View.VISIBLE else View.GONE
    }

    private fun checkIfEdited(): Boolean {
        if( (binding.editProfileName.text.toString() != viewModel.name) || (binding.editProfileEmail.text.toString() != viewModel.email)
            || (binding.editProfilePhonenum.text.toString() != viewModel.phone) || (binding.editProfileAddress.text.toString() != viewModel.address)
            || (binding.editProfileAge.text.toString() != viewModel.age) || (binding.editProfileGender.text.toString() != viewModel.gender)
            || (binding.editProfileAge.text.toString() == "") || (binding.editProfileGender.text.toString() == "")){
            return true
        } else {
            return false
        }
    }



    private fun retrieveImage(userid : String) {
        RetriveImg.retrieveImage(userid, binding.profileImage)
    }



}
