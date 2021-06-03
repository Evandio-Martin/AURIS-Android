package com.bangkit.auris.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bangkit.auris.utils.DataDummy
import androidx.recyclerview.widget.GridLayoutManager
import com.bangkit.auris.adapter.DataAdapter
import com.bangkit.auris.databinding.FragmentAlphabetBinding

class AlphabetFragment : Fragment() {

    private lateinit var fragmentAlphabetBinding: FragmentAlphabetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentAlphabetBinding = FragmentAlphabetBinding.inflate(layoutInflater, container, false)
        return fragmentAlphabetBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (activity != null){
            val library = DataDummy.generateDummyAlphabet()
            val dataAdapter = DataAdapter()
            dataAdapter.setDataLibrary(library)

            with(fragmentAlphabetBinding.rvAlphabet){
                layoutManager = GridLayoutManager(context, 2)
                setHasFixedSize(true)
                adapter = dataAdapter
            }
        }
    }
}