package com.tehgan.phylaunch.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.tehgan.phylaunch.BaseApplication
import com.tehgan.phylaunch.R
import com.tehgan.phylaunch.broadcast.PackageReceiver
import com.tehgan.phylaunch.databinding.FragmentPagerBinding
import com.tehgan.phylaunch.datastore.AppDockDataStore
import com.tehgan.phylaunch.model.LauncherViewModel
import com.tehgan.phylaunch.model.LauncherViewModelFactory
import com.tehgan.phylaunch.ui.edit.AddAppDialogFragment
import com.tehgan.phylaunch.ui.home.HomeFragment
import kotlinx.coroutines.launch

const val NUM_PAGES = 5

/**
 * Holds the ViewPager2, for navigating between 'pages' (instances of HomeFragment)
 */
class HomePager : Fragment() {

    private lateinit var binding: FragmentPagerBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var packageReceiver: PackageReceiver

    private val appDockDataStore by lazy { AppDockDataStore(requireContext()) }
    private lateinit var viewModel: LauncherViewModel

    private var initialized: Boolean = false

    interface PageSwitchCallback {
        fun switchPage(direction: Char, position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean("init")) {
                initialized = true
            }
        }

        viewModel = ViewModelProvider(this, LauncherViewModelFactory(
            (activity?.application as BaseApplication)
                .database.launcherDao())
        )[LauncherViewModel::class.java]

        packageReceiver = PackageReceiver(viewModel)

    }

    // App has been initialized
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("init", true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPagerBinding.inflate(layoutInflater)

        setWallpaper()

        viewPager = binding.pager
        // Default offscreenPageLimit seems to make focusing buggy
        viewPager.offscreenPageLimit = 1
        val adapter = HomePager(requireActivity())
        viewPager.adapter = adapter

        val dotIndicator = binding.dotIndicator

        // Listen for AddAppDialogFragment, take selected app and insert into dock
        childFragmentManager.setFragmentResultListener("pkgDock", viewLifecycleOwner) { _, bundle ->
            val pos = bundle.getInt("pos")
            val name = bundle.getString("name")

            if (name != null) {
                lifecycleScope.launch {
                    appDockDataStore.saveApp(pos, name)
                }

            } else {
                Log.e("HomePager", "pkgDock result received, but name is null!")
            }

        }

        // TODO: This code may be bad/hacky.
        //  Does it take the keyEvent from other apps (e.g. accessibility)?
        /* Disable left D-Pad navigation on left-most cell,
         *  to ensure user can't switch focus to invisible pages */
        binding.dockApp1.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == 21) { // D-Pad Left
                return@setOnKeyListener true
            }
            false
        }
        // Ditto for right D-Pad
        binding.dockApp4.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == 22) { // D-Pad Right
                return@setOnKeyListener true
            }
            false
        }

        // Automatically update dock on removal or insertion of app
        appDockDataStore.appDockFlow.asLiveData().observe(this.viewLifecycleOwner) { apps ->
            setDock(apps)
        }

        binding.appDrawerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homePager_to_appDrawerFragment)
        }

        // Update dot indicator (change opacity of currently selected page's corresponding dot)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                dotIndicator.updateDot(position)
            }
        })
        
        return binding.root
    }

    override fun onResume() {

        // TODO: Workaround; will also focus appDrawerBtn when resuming from sleep, which we don't want.
        //  Find way to save/load existing focused cell, and re-focus if it existed and was valid.
        binding.appDrawerBtn.requestFocus()

        super.onResume()

        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
        filter.addDataScheme("package")
        requireContext().registerReceiver(packageReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(packageReceiver)
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class HomePager(fa: FragmentActivity
    ) : FragmentStateAdapter(fa) {

        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment = HomeFragment.newInstance(
            position,
            initialized
        )

    }

    @SuppressLint("MissingPermission")
    private fun setWallpaper() {

        // TODO: Fix handling of perm denial, app doesn't ask again for whatever reason
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("HomePager", "Permission was granted")
                // You can use the API that requires the permission.
                val wallpaperManager = WallpaperManager.getInstance(requireContext())
                if (wallpaperManager != null) {
                    binding.pager.background = wallpaperManager.drawable
                } else {
                    return
                }
            }
            // TODO: Need to find a way to 'throttle', so that the user doesn't get spammed whenever
            //  resuming the app, leaving the AppDrawer, etc.
            /*ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                Log.d("HomePager", "Showing rationale")
                // Perms were denied. Explain why user should allow external storage permissions.
                PermissionsInfoDialog().show(childFragmentManager, PermissionsInfoDialog.TAG)
            }*/
            else -> {
                // Request permission
                Log.d("HomePager", "Attempting to request perms")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    74951
                )
            }
        }

    }

    private fun setDock(dockApps: List<String>) {
        val pm = requireContext().packageManager

        val dApps = listOf(binding.dockApp1, binding.dockApp2, binding.dockApp3, binding.dockApp4)
        dApps.forEachIndexed { index, app ->
            val appInfo = try {
                pm.getApplicationInfo(dockApps[index], 0)
            } catch (e: NameNotFoundException) {
                null
            }

            if (appInfo != null) {
                val launchIntent: Intent? = pm.getLaunchIntentForPackage(dockApps[index])
                if (launchIntent != null) {
                    app.appLabel.text = pm.getApplicationLabel(appInfo)
                    app.appIcon.setImageDrawable(pm.getApplicationIcon(appInfo))
                    app.root.setOnClickListener { startActivity(launchIntent) }
                    app.root.setOnLongClickListener { // Delete app
                        lifecycleScope.launch { appDockDataStore.deleteApp(index) }
                        return@setOnLongClickListener true
                        }
                    }

            } else {
                // app.root.isFocusable = false
                app.appLabel.text = null
                app.appIcon.setImageDrawable(null)
                // Add app
                app.root.setOnClickListener {
                    AddAppDialogFragment(index, 1)
                        .show(childFragmentManager, AddAppDialogFragment.TAG) }
            }
        }
    }

}