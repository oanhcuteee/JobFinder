package com.example.jobfinder.UI.Login

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class UsersDataSavedModel(
    @PrimaryKey val email: String,
    val password: String,
    var isBiometricEnabled: Boolean = false
)
