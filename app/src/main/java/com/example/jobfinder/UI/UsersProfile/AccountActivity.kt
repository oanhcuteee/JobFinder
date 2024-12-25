package com.example.jobfinder.UI.UsersProfile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.Datas.Model.idAndRole
import com.example.jobfinder.Utils.FragmentHelper
import com.example.jobfinder.Utils.GetData
import com.example.jobfinder.databinding.ActivityAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountBinding
    private lateinit var auth: FirebaseAuth
    private var userRole: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //firebase
        auth = FirebaseAuth.getInstance()
        val uid = GetData.getCurrentUserId()

        FirebaseDatabase.getInstance().getReference("UserRole").child(uid.toString()).get()
            .addOnSuccessListener { snapshot ->
                val data: idAndRole? = snapshot.getValue(idAndRole::class.java)
                data?.let {
                    userRole = data.role.toString()
                    if (userRole=="NUser"){
                        FragmentHelper.replaceFragment(supportFragmentManager , binding.profileframelayout, SeekerEditProfileFragment())
                        binding.animationView.visibility = View.GONE
                    }else if (userRole=="BUser"){
                        FragmentHelper.replaceFragment(supportFragmentManager , binding.profileframelayout, RecruterEditProfileFragment())
                        binding.animationView.visibility = View.GONE
                    }
                }
            }
    }
}