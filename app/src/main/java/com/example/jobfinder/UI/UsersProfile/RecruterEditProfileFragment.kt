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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.jobfinder.R
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.Utils.VerifyField
import com.example.jobfinder.databinding.FragmentRecruterEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RecruterEditProfileFragment : Fragment() {
    private lateinit var binding: FragmentRecruterEditProfileBinding
    private lateinit var auth: FirebaseAuth
    lateinit var viewModel: ProfileViewModel
    private var BusTypeChoosed = false
    private var BusSecChoosed = false


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
                    Log.e("RecruterEditProfileFragment", "Database error: ${error.message}")
                }
            })
            database.child("BUserInfo").child(it).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tax = snapshot.child("tax_code").getValue(String::class.java)
                    tax?.let {
                        viewModel.tax = it
                        if(it == ""){
                            binding.editProfileTaxnum.setHint(R.string.blank_tax_code)
                        }else {
                            binding.editProfileTaxnum.setText(viewModel.tax)
                        }
                    }
                    val description = snapshot.child("description").getValue(String::class.java)
                    description?.let {
                        viewModel.des = it
                        if(it == ""){
                            binding.editProfileDescription.setHint(R.string.no_job_des2)
                        }else {
                            binding.editProfileDescription.setText(viewModel.des)
                        }
                    }
                    val busType = snapshot.child("business_type").getValue(String::class.java)
                    busType?.let {
                        viewModel.busType = it
                        if(it == ""){
                            binding.editProfileBustype.setHint(R.string.error_invalid_BusSec)
                        }else {
                            binding.editProfileBustype.setText(viewModel.busType)
                        }
                    }
                    val busSec = snapshot.child("business_sector").getValue(String::class.java)
                    busSec?.let {
                        viewModel.busSec = it
                        if(it == ""){
                            binding.editProfileBusSec.hint = getString(R.string.blank_sector)
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


        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecruterEditProfileBinding.inflate(inflater, container, false)
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
        val description = binding.editProfileDescription
        val save = binding.editProfileSave
        val busType = binding.editProfileBustype
        val busSec = binding.editProfileBusSec
        val tax = binding.editProfileTaxnum
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
                        binding.editProfileTaxnum.setText(viewModel.tax)
                        binding.editProfileDescription.setText(viewModel.des)
                        binding.editProfileBustype.text = viewModel.busType
                        binding.editProfileBusSec.text = viewModel.busSec
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

        // button lưu
        save.setOnClickListener {
            val newName = name.text.toString()
            val newAddress = address.text.toString()
            val newPhone = phone.text.toString()
            val newDes = description.text.toString()
            val newTax = tax.text.toString()
            val newBusType = busType.text.toString()
            val newBusSec = busSec.text.toString()

            val isValidName = VerifyField.isValidName(newName)
            val isValidAddress = newAddress.isNotEmpty()
            val isValidPhone = VerifyField.isValidPhoneNumber(newPhone)
            val isValidDes = newDes.isNotEmpty()
            val isValidTax = VerifyField.isValidTaxCode(newTax)
            val isValidBusType = newBusType.isNotEmpty()
            val isValidBusSec = newBusSec.isNotEmpty()


            name.error = if (isValidName) null else getString(R.string.error_invalid_name)
            address.error = if (isValidAddress) null else getString(R.string.error_invalid_addr)
            phone.error = if (isValidPhone) null else getString(R.string.error_invalid_hotline)
            description.error = if (isValidDes) null else getString(R.string.error_invalid_des)
            busType.error = if (isValidBusType) null else getString(R.string.error_invalid_BusType)
            busSec.error = if (isValidBusSec) null else getString(R.string.error_invalid_BusSec)
            tax.error = if (isValidTax) null else getString(R.string.error_invalid_Tax)


            if( checkIfEdited() == true) {
                if (isValidName && isValidAddress && isValidPhone && isValidDes && isValidBusType && isValidBusSec && isValidTax) {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    userId?.let {

                        viewModel.name = newName
                        viewModel.address = newAddress
                        viewModel.phone = newPhone
                        viewModel.des = newDes
                        viewModel.tax= newTax
                        viewModel.busType = newBusType
                        viewModel.busSec = newBusSec

                        val userBI = FirebaseDatabase.getInstance().reference.child("UserBasicInfo").child(it)
                        val Buser = FirebaseDatabase.getInstance().reference.child("BUserInfo").child(it)
                        userBI.child("name").setValue(newName)
                        userBI.child("address").setValue(newAddress)
                        userBI.child("phone_num").setValue(newPhone)
                        Buser.child("description").setValue(newDes)
                        Buser.child("tax_code").setValue(newTax)
                        Buser.child("business_sector").setValue(newBusSec)
                        Buser.child("business_type").setValue(newBusType)

                        Toast.makeText(requireContext(), getString(R.string.profile_edited), Toast.LENGTH_SHORT).show()

                    }

                    isEdited = false
                    disableButton(isEdited)
                } else {
                    checkToAutoFocus(isValidName , isValidAddress , isValidPhone , isValidDes , isValidBusType , isValidBusSec , isValidTax)
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
                val newDes = description.text.toString()
                val newTax = tax.text.toString()
                val newBusType = busType.text.toString()
                val newBusSec = busSec.text.toString()
                val isValidDes = newDes.isNotEmpty()
                val isValidTax = VerifyField.isValidTaxCode(newTax)
                val isValidBusType = newBusType.isNotEmpty()
                val isValidBusSec = newBusSec.isNotEmpty()
                description.error = if (isValidDes) null else getString(R.string.error_invalid_des)
                busType.error = if (isValidBusType) null else getString(R.string.error_invalid_BusType)
                busSec.error = if (isValidBusSec) null else getString(R.string.error_invalid_BusSec)
                tax.error = if (isValidTax) null else getString(R.string.error_invalid_Tax)
                if(isValidDes && isValidBusType && isValidBusSec && isValidTax){
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

        busType.setOnClickListener {
            BusTypeSelect(it)
            BusTypeChoosed = true
        }

        busSec.setOnClickListener {
            BusSecSelect(it)
            BusSecChoosed = true
        }

        binding.uploadImage.setOnClickListener() {
            val intent = Intent(requireContext(), profile_upload_image::class.java)
            startActivity(intent)
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

    private fun BusTypeSelect(view: View){
        showPopupMenu(view, R.menu.business_type_menu) { selectedBusType ->
            binding.editProfileBustype.text = selectedBusType
        }
    }
    private fun BusSecSelect(view: View){
        showPopupMenu(view, R.menu.business_sector_menu) { selectedBusSec ->
            binding.editProfileBusSec.text = selectedBusSec
        }
    }

    private fun disableButton(isEdited : Boolean){
        binding.editProfileName.isEnabled = isEdited
        binding.editProfileAddress.isEnabled = isEdited
        binding.editProfilePhonenum.isEnabled = isEdited
        binding.editProfileDescription.isEnabled = isEdited
        binding.editProfileBustype.isEnabled = isEdited
        binding.editProfileBusSec.isEnabled = isEdited
        binding.editProfileTaxnum.isEnabled = isEdited
        binding.uploadImage.visibility = if (isEdited) View.VISIBLE else View.GONE
        binding.editProfileSave.visibility = if (isEdited) View.VISIBLE else View.GONE
    }

    private fun checkIfEdited(): Boolean {
        if( (binding.editProfileName.text.toString() != viewModel.name) || (binding.editProfileEmail.text.toString() != viewModel.email)
            || (binding.editProfilePhonenum.text.toString() != viewModel.phone) || (binding.editProfileAddress.text.toString() != viewModel.address)
            || (binding.editProfileTaxnum.text.toString() != viewModel.tax) || (binding.editProfileDescription.text.toString() != viewModel.des)
            || (binding.editProfileBusSec.text.toString() != viewModel.busSec) || (binding.editProfileBustype.text.toString() != viewModel.busType)
            || (binding.editProfileTaxnum.text.toString() == "") || (binding.editProfileDescription.text.toString() == "")
            || (binding.editProfileBusSec.text.toString() == "") || (binding.editProfileBustype.text.toString() == "")){
                return true
        } else {
            return false
        }
    }

    private fun checkToAutoFocus(vararg isValidFields: Boolean) {
        val invalidFields = mutableListOf<EditText>()
        val invalidField = mutableListOf<TextView>()
        for ((index, isValid) in isValidFields.withIndex()) {
            if (!isValid) {
                when (index) {
                    0 -> invalidFields.add(binding.editProfileName)
                    1 -> invalidFields.add(binding.editProfilePhonenum)
                    2 -> invalidFields.add(binding.editProfileAddress)
                    3 -> invalidFields.add(binding.editProfileTaxnum)
                    4 -> invalidFields.add(binding.editProfileDescription)
                    5 -> invalidField.add(binding.editProfileBustype)
                    6 -> invalidField.add(binding.editProfileBusSec)
                }
            }
        }

        if (invalidFields.isNotEmpty()) {
            invalidFields.first().requestFocus()
        }
    }
    //fetch ảnh
    private fun retrieveImage(userid : String) {
        RetriveImg.retrieveImage(userid, binding.profileImage)
    }

}