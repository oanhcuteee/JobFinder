package com.example.jobfinder.UI.Report

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobfinder.Datas.Model.SupportUser
import com.example.jobfinder.R
import com.example.jobfinder.UI.JobPosts.JobTypeSpinner
import com.example.jobfinder.databinding.ActivitySupportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupportBinding
    private lateinit var auth: FirebaseAuth
    private var statusType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        var check = ""
        binding.requestForm.visibility = INVISIBLE
        binding.btnWrapper.visibility = INVISIBLE

        binding.backButton.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.technical.setOnClickListener {
            handleButtonClick(R.string.create_technical_report, R.array.technical_array)
            binding.technical.setBackgroundColor(getColor(R.color.primary_color3))
            check = "technical"
        }
        binding.feedback.setOnClickListener {
            handleButtonClick(R.string.create_feedback, R.array.feedback_array)
            binding.feedback.setBackgroundColor(getColor(R.color.primary_color3))
            check = "feedback"
        }
        binding.report.setOnClickListener {
            handleButtonClick(R.string.create_user_report, R.array.report_array)
            binding.report.setBackgroundColor(getColor(R.color.primary_color3))
            check = "report"
        }

        binding.saveChange.setOnClickListener(){
            val note = binding.note.text.toString()

            if (check != ""){
                val supportId = FirebaseDatabase.getInstance().getReference("Support").child(userId.toString()).push().key
                val reportForm =  SupportUser(supportId, check, statusType, note, userId)
                FirebaseDatabase.getInstance().getReference("AdminRef").child("Report").child(supportId.toString()).setValue(reportForm)
                Toast.makeText(this, getString(R.string.request_sent), Toast.LENGTH_SHORT).show()
                binding.note.setText("")

            } else {
                Toast.makeText(this, getString(R.string.request_sent_error), Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun handleButtonClick(titleResId: Int, arrayResId: Int) {
        binding.requestName.setText(titleResId)
        binding.technical.setBackgroundColor(getColor(android.R.color.transparent))
        binding.feedback.setBackgroundColor(getColor(android.R.color.transparent))
        binding.report.setBackgroundColor(getColor(android.R.color.transparent))
        setupSpinner(resources.getStringArray(arrayResId))
        binding.requestForm.visibility = VISIBLE
        binding.btnWrapper.visibility = VISIBLE
        binding.note.setText("")
    }

    private fun setupSpinner(statusArray: Array<String>) {
        val spinner = binding.status
        val spinnerAdapter = JobTypeSpinner(binding.root.context, statusArray)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                statusType = parent?.getItemAtPosition(position).toString()
                handleSelectedJobType(statusType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Xử lý khi không có mục nào được chọn
            }
        }
    }

    private fun handleSelectedJobType(statusType: String) {
        Log.d("Selected Status Type", statusType)
    }

}