package com.bangkit.auris.viewmodel

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory {
    fun <T : ViewModel?> createViewModel(
        activity: FragmentActivity,
        application: Application,
        cls: Class<T>?
    ): T {
        return ViewModelProvider(
            activity,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(cls!!)
    }
}