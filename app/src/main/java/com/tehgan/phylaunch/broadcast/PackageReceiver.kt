package com.tehgan.phylaunch.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tehgan.phylaunch.model.LauncherViewModel
import kotlinx.coroutines.runBlocking

// Removes uninstalled packages from database
class PackageReceiver(private val viewModel: LauncherViewModel) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
            val packageName = intent.data?.encodedSchemeSpecificPart
            if (packageName != null) {
                // TODO: Find alternative to runBlocking, maybe?
                runBlocking {
                    viewModel.searchAndDestroy(packageName)
                }
            }
        }
    }
}