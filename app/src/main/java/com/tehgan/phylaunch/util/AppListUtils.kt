package com.tehgan.phylaunch.util

import android.content.Context
import android.content.pm.ApplicationInfo

class AppListUtils {
    
    // Get a filtered (via LaunchIntent null-checking) list of applications, sorted alphabetically
    fun getApps(context: Context): List<ApplicationInfo> {
        println("Filtering apps")

        // Get list of installed applications
        val pm = context.packageManager
        val appList = pm.getInstalledApplications(0)

        // Filter out non-launchable applications (e.g. System UI)
        val fAppList = mutableListOf<ApplicationInfo>()
        for (app in appList) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                fAppList.add(app)
            }
        }

        // Return alphabetically sorted app list
        return fAppList.sortedBy { it.loadLabel(pm).toString() }
    }

}