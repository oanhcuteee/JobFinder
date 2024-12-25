package com.example.jobfinder.UI.Login

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import com.example.jobfinder.R

class NotifyBiometricDialog(context: Context) : Dialog(context) {
    init {
        setContentView(R.layout.dialog_biometric_notification)
        findViewById<TextView>(R.id.dismissButton).setOnClickListener {
            dismiss()
        }
    }
}