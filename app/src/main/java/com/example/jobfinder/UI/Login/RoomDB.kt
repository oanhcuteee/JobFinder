package com.example.jobfinder.UI.Login

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [UsersDataSavedModel::class], version = 1)
abstract class RoomDB : RoomDatabase() {
    abstract fun usersDao(): UsersDao

    companion object {
        @Volatile private var INSTANCE: RoomDB? = null

        fun getDatabase(context: Context): RoomDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}