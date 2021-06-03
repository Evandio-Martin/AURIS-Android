package com.bangkit.auris.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bangkit.auris.adapter.SectionsPagerAdapter
import com.bangkit.auris.viewmodel.PageViewModel
import com.bangkit.auris.viewmodel.ViewModelFactory
import com.bangkit.auris.R
import com.bangkit.auris.databinding.FragmentDictionaryBinding

class DictionaryFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentDictionaryBinding

    companion object {
        fun newInstance(): DictionaryFragment {
            return DictionaryFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Instantiate ViewBinding
        this.binding = FragmentDictionaryBinding.inflate(inflater, container, false)

        val sectionsPagerAdapter = this.activity?.let {
            SectionsPagerAdapter(
                this.requireContext(),
                it.supportFragmentManager
            )
        }
        binding.viewPager.adapter = sectionsPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        //Return current Fragment
        return this.binding.root
    }
}