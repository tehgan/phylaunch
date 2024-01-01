package com.tehgan.phylaunch.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LauncherInfo::class], version = 1, exportSchema = false)
abstract class LauncherDatabase : RoomDatabase() {

    abstract fun launcherDao(): LauncherDao

    companion object {
        private val TAG = "LauncherDatabase"

        @Volatile
        private var INSTANCE: LauncherDatabase? = null

        fun getDatabase(context: Context): LauncherDatabase {
            Log.d(TAG, "Getting database...")
            // return database, or if not exists, create it.
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Database doesn't exist. Creating.")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LauncherDatabase::class.java,
                    "home_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

}