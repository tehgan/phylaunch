package com.tehgan.phylaunch.ui.appdrawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tehgan.phylaunch.databinding.FragmentAppdrawerBinding
import com.tehgan.phylaunch.util.AppListUtils

class AppDrawerFragment : Fragment() {

    private var _binding: FragmentAppdrawerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppdrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvAdapter = AppDrawerAdapter(AppListUtils().getApps(requireContext()))
        binding.recyclerView.adapter = rvAdapter
    }

}