package com.example.jobfinder.UI.UsersProfile

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobfinder.R
import com.example.jobfinder.UI.SplashScreen.SelectRoleActivity
import com.example.jobfinder.Utils.RetriveImg
import com.example.jobfinder.databinding.FragmentUserProfileMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class UserProfileMenuFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileMenuBinding
    private lateinit var auth: FirebaseAuth
    lateinit var viewModel: ProfileViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileMenuBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hiển thị username
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid
        userId?.let { userId ->
            database.child("UserBasicInfo").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java)
                    userName?.let {
                        binding.userName.text = it
                        binding.animationView.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileMenuFragment", "Database error: ${error.message}")
                }
            })

            retrieveImage(userId)
        }

        //account
        binding.profileAccount.setOnClickListener {
            val intent = Intent(requireContext(), AccountActivity::class.java)
            startActivity(intent)

        }

        //settings
        binding.profileSettings.setOnClickListener(){
            val intent = Intent(requireContext(),SettingsActivity::class.java)
            startActivity(intent)
        }

        //logout
        binding.profileLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), SelectRoleActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(context, R.string.profile_logout_toast, Toast.LENGTH_SHORT).show()
            requireActivity().finishAffinity()

        }




    }
    private fun retrieveImage(userid : String) {
        RetriveImg.retrieveImage(userid, binding.profileImage)
    }





}