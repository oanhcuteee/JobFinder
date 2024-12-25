package com.example.jobfinder.UI.Login

import androidx.room.*

@Dao
interface UsersDao { // Dao: database access object
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UsersDataSavedModel)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UsersDataSavedModel?

    @Update
    suspend fun updateUser(user: UsersDataSavedModel)
}