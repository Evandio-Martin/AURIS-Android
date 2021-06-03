package com.bangkit.auris.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bangkit.auris.R
import com.bangkit.auris.databinding.ActivitySplashScreenBinding

class LogoFragment : Fragment(R.layout.activity_splash_screen) {
    private lateinit var binding: ActivitySplashScreenBinding

    companion object {
        fun newInstance(): LogoFragment {
            return LogoFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Instantiate ViewBinding
        this.binding = ActivitySplashScreenBinding.inflate(inflater, container, false)

        //Return current Fragment
        return this.binding.root
    }
}