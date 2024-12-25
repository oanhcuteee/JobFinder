package com.example.jobfinder.UI.UsersProfile

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobfinder.R
import com.example.jobfinder.databinding.ActivityProfileUploadImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class profile_upload_image : AppCompatActivity() {
    private lateinit var binding: ActivityProfileUploadImageBinding
    private lateinit var auth: FirebaseAuth
    lateinit var viewModel: ProfileViewModel
    var fileUri: Uri? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding = ActivityProfileUploadImageBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        setContentView(binding.root)

        userId?.let { userId ->
            retrieveImage(userId)
        }
        //back
        binding.uploadImageBackbtn.setOnClickListener(){
            back()
        }
        //choose image
        binding.profileImageChoose.setOnClickListener(){
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, ""), 0
            )

        }
        //save image to firebase
        binding.profileImageSave.setOnClickListener(){
            if (fileUri != null) {
                userId?.let {
                    uploadImage(userId)
                }

            } else {
                Toast.makeText(
                    applicationContext, R.string.profile_image_please_choose,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        //discard
        binding.profileImageDiscard.setOnClickListener(){
            showDeleteConfirmationDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK && data != null && data.data != null) {
            fileUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                Glide.with(this)
                    .load(bitmap)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.profileImage)

            } catch (e: Exception) {
                Log.e("Exception", "Error: " + e)
            }
        }
    }
    private fun uploadImage(userid: String) {

        if (fileUri != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle(R.string.profile_image_uploading)
            progressDialog.setMessage(getString(R.string.profile_image_process))
            progressDialog.show()

            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child(userid)
            ref.putFile(fileUri!!).addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, R.string.profile_image_save_success, Toast.LENGTH_LONG)
                    .show()
                back()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, R.string.profile_image_save_failed, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun back(){
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.profile_image_delete_confim)
        builder.setMessage(R.string.profile_image_delete_confim_noti)
        builder.setPositiveButton(R.string.profile_image_delete_confim_yes) { dialog, which ->
            val userId = auth.currentUser?.uid
            userId?.let {
                deleteImage(userId)
            }
        }
        builder.setNegativeButton(R.string.profile_image_delete_confim_no) { dialog, which ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }



    private fun deleteImage(userId: String) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
        val imageRef: StorageReference = storageReference.child(userId)
        // gỡ ảnh hiện tại
        imageRef.delete().addOnSuccessListener {
            Glide.with(this)
                .load(imageRef)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.profileImage)
                .clearOnDetach()

            Toast.makeText(
                applicationContext, R.string.profile_image_deleted_success,
                Toast.LENGTH_LONG
            ).show()
            back()

            //thay bằng ảnh default
            binding.profileImage.setBackgroundResource(R.drawable.profile)
        }.addOnFailureListener {
            Toast.makeText(
                applicationContext, R.string.profile_image_delete_failed,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun retrieveImage(userid : String) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val imageRef: StorageReference = storageReference.child(userid)
        Log.d("SeekerEditProfileFragment", "ImageRef path: $imageRef")

        // gỡ ảnh hiện tại
        Glide.with(this)
            .load(imageRef)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.profileImage)
            .clearOnDetach()
        // gắn ảnh mới đã fetch vào
        imageRef.downloadUrl
            .addOnSuccessListener { uri: Uri ->
                binding.profileImage.setBackgroundResource(R.drawable.image_loading_80px)
                viewModel.imageUri = uri
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.profileImage)
                binding.animationView.visibility = View.GONE


            }
            .addOnFailureListener { exception ->
                Log.e("UserProfileMenuFragment", "Failed to retrieve image: ${exception.message}")
                binding.profileImage.setBackgroundResource(R.drawable.profile)
                binding.animationView.visibility = View.GONE

            }
    }




}