package com.tehgan.phylaunch.ui.edit

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.tehgan.phylaunch.databinding.DialogAddAppBinding
import com.tehgan.phylaunch.util.AppListUtils

// Dialog to appear when user wants to add an app to the homescreen
class AddAppDialogFragment(private val pos: Int, private val callingFrom: Int) : DialogFragment() {

    private var _binding: DialogAddAppBinding? = null
    private val binding get() = _binding!!

    private lateinit var appList: List<ApplicationInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        // Prevent visible (yet blank) title bar on certain Android versions and/or devices
        setStyle(STYLE_NO_TITLE, theme)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogAddAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appList = AppListUtils().getApps(requireContext())

        val homeCallback: (String) -> Unit = { name ->
            setFragmentResult("pkgHome", bundleOf(
                "pos" to pos,
                "name" to name
            ))
            dismiss()
        }

        val dockCallback: (String) -> Unit = { name ->
            setFragmentResult("pkgDock", bundleOf(
                "pos" to pos,
                "name" to name
            ))
            dismiss()
        }

        val stubCallback: (String) -> Unit = { dismiss() }

        binding.recyclerView.adapter = when (callingFrom) {
            0 -> { AddAppDialogAdapter(appList, homeCallback) }
            1 -> { AddAppDialogAdapter(appList, dockCallback) }
            else -> { AddAppDialogAdapter(appList, stubCallback) }
        }
    }

    companion object {
        const val TAG = "AddAppDialogFragment"
    }

}
