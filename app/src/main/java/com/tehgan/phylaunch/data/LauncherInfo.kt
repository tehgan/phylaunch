package com.tehgan.phylaunch.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_db")
data class LauncherInfo(
    @PrimaryKey
    val position: Int,
    @ColumnInfo(name = "package_name")
    val packageName: String,
    @ColumnInfo(name = "package_label")
    val packageLabel: String, // User-friendly app name
)