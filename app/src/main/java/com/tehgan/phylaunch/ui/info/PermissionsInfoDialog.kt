package com.tehgan.phylaunch.ui.info

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.tehgan.phylaunch.R

// Dialog to appear when user denies permission to external storage
class PermissionsInfoDialog() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.perms_notification))
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
            .create()

    companion object {
        const val TAG = "PermissionsInfoDialog"
    }

}
