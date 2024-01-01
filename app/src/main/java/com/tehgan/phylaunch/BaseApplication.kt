package com.tehgan.phylaunch

import android.app.Application
import com.tehgan.phylaunch.data.LauncherDatabase

// Creates a singleton instance of the LauncherDatabase
class BaseApplication : Application() {
    val database: LauncherDatabase by lazy { LauncherDatabase.getDatabase(this) }
}