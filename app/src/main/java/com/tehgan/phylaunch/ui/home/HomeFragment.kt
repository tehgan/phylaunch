package com.tehgan.phylaunch.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.viewpager2.widget.ViewPager2
import com.tehgan.phylaunch.BaseApplication
import com.tehgan.phylaunch.R
import com.tehgan.phylaunch.data.LauncherInfo
import com.tehgan.phylaunch.databinding.FragmentHomeBinding
import com.tehgan.phylaunch.model.LauncherViewModel
import com.tehgan.phylaunch.model.LauncherViewModelFactory
import com.tehgan.phylaunch.ui.HomePager
import com.tehgan.phylaunch.ui.edit.AddAppDialogFragment

// Number of cells per page (currently 20 for 4*5 grid)
private const val CELL_COUNT = 20

class HomeFragment : Fragment(), HomePager.PageSwitchCallback {

    /* Initializer w/ pageNum value.                                                        *
     * Can't pass value directly to constructor, or app crashes after sleep & wake due to   *
     * "InstantiationException: could not find Fragment constructor" error.                 */
    companion object {
        fun newInstance(pageNum: Int, initialized: Boolean): HomeFragment {
            val args = Bundle()
            args.putInt("pageNum", pageNum)
            args.putBoolean("init", initialized)
            val hf = HomeFragment()
            hf.arguments = args
            return hf
        }
    }

    // TODO: Does this need to be an interface, or can it be a hf-native function?
    /**
     * Page-switcher, to be called from HomeAdapter.
     * Switches page left/right, and focuses RecyclerView cell accordingly.
     */
    override fun switchPage(direction: Char, position: Int) {
        val vp2 = requireActivity().findViewById<ViewPager2>(R.id.pager)
        val curItem = vp2.currentItem

        if (direction == 'l' && requireArguments().getInt("pageNum") > 0) {
            vp2.currentItem = (curItem - 1)

            val curFragment = parentFragmentManager
                .findFragmentByTag("f${vp2.currentItem}")
            if (curFragment is HomeFragment) {
                curFragment.setFocus(position + 3)
            }

        } else if (direction == 'r' && requireArguments().getInt("pageNum") < 4) {
            vp2.currentItem = (curItem + 1)

            val curFragment = parentFragmentManager
                .findFragmentByTag("f${vp2.currentItem}")
            if (curFragment is HomeFragment) {
                curFragment.setFocus(position - 3)
            }
        }

    }

    private var pageNum: Int = -1
    private lateinit var viewModel: LauncherViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var rvAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            pageNum = requireArguments().getInt("pageNum");
        } else {
            Toast.makeText(requireContext(),
                "PhyLaunch: Page creation error. Please file a GitHub report.", Toast.LENGTH_LONG).show()
            requireActivity().finishAndRemoveTask()
        }

        // TODO: Check if multiple database instances are being created
        // TODO: Do we need the ViewModelProvider scoped to activity, if we're creating several?
        viewModel = ViewModelProvider(this, LauncherViewModelFactory(
            (activity?.application as BaseApplication)
            .database.launcherDao())
        )[pageNum.toString(), LauncherViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Observe current page of apps, populate with blank cells (if required) when modified
        viewModel.getPage(pageNum).observe(this.viewLifecycleOwner) { apps ->
            viewModel.populateAppList(apps, pageNum)
        }

        // Listen for app selection/addition bundle from AddAppDialog
        childFragmentManager.setFragmentResultListener("pkgHome", viewLifecycleOwner) { _, bundle ->
            val pos = bundle.getInt("pos")
            val name = bundle.getString("name")
            val label: String

            if (name != null) {
                val pm = requireContext().packageManager
                label = pm.getApplicationLabel(pm.getApplicationInfo(name, 0)).toString()
                viewModel.addApp(pos + (pageNum * CELL_COUNT), name, label)

            } else {
                Log.e("HomeFragment", "pkgHome result received, but name or label are null!")
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deleteListener: (LauncherInfo) -> Unit = { li ->
            val app = LauncherInfo(
                position = (li.position + (pageNum * CELL_COUNT)),
                packageName = li.packageName,
                packageLabel = li.packageLabel
            )
            viewModel.deleteApp(app)
        }

        val addListener: (Int) -> Unit = { pos ->
            // CELL_COUNT/pageNum calculation code factored into FragmentResultListener
            AddAppDialogFragment(pos, 0).show(childFragmentManager, AddAppDialogFragment.TAG)
        }

        rvAdapter = HomeAdapter(addListener, deleteListener)
        rvAdapter.callback = this // TODO: Pass as parameter, or keep this way?
        binding.recyclerView.adapter = rvAdapter

        /* pagedAppList contains apps (called via getPage) combined with blank cells (to fill the RecyclerView).
             It's updated at the same time as getPage, thanks to an observer called early in onCreateView()
             executing viewModel.populateAppList() on update. */
        viewModel.pagedAppList.asLiveData().observe(this.viewLifecycleOwner) { apps ->
            apps.let {
                rvAdapter.submitList(it)
            }
        }

    }

    fun setFocus(pos: Int) {
        binding.recyclerView.getChildAt(pos).requestFocus()
    }

}