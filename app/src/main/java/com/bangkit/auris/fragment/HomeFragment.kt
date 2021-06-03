package com.bangkit.auris.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bangkit.auris.viewmodel.PageViewModel
import com.bangkit.auris.viewmodel.ViewModelFactory
import com.bangkit.auris.R
import com.bangkit.auris.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var pageViewModel: PageViewModel

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Instantiate ViewBinding
        this.binding = FragmentHomeBinding.inflate(inflater, container, false)

        //Instantiate Page ViewModel
        this.pageViewModel = activity?.run {
            ViewModelFactory().createViewModel(this, application, PageViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        //Return current Fragment
        return this.binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.binding.btnHome.setOnClickListener{
            this.pageViewModel.toggleProcessingStatus()
        }

        this.pageViewModel.getProcessingStatus().observe(viewLifecycleOwner, {
            if (it == true) {
                this.binding.btnHome.text = getString(R.string.btn_processing_stop)
            }
            else {
                this.binding.btnHome.text = getString(R.string.btn_processing_start)
            }
        })

        this.pageViewModel.getHighestDetected().observe(viewLifecycleOwner, {
            this.binding.tvHome.text = it
        })
    }
}