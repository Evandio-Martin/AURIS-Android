package com.bangkit.auris.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PageViewModel(application: Application) : AndroidViewModel(application) {
    private val page: MutableLiveData<String> = MutableLiveData()
    private val isProcessing: MutableLiveData<Boolean> = MutableLiveData()
    private val isShowingPreview: MutableLiveData<Boolean> = MutableLiveData()
    val highestDetected: MutableLiveData<String> = MutableLiveData()

    init {
        this.isProcessing.value = false
        this.isShowingPreview.value = false
        this.highestDetected.value = ""
        this.changePage("HOME")
    }

    fun getPage() = this.page as LiveData<String>
    fun getProcessingStatus() = this.isProcessing as LiveData<Boolean>
    fun getShowingPreviewStatus() = this.isShowingPreview as LiveData<Boolean>
    fun getHighestDetected() = this.highestDetected as LiveData<String>
  
    fun changePage(pageName: String) {
        this.page.value = pageName
    }
    fun toggleProcessingStatus() {
        this.isProcessing.value = !this.isProcessing.value!!
    }
    fun changeProcessingStatus(status: Boolean) {
        this.isProcessing.value = status
    }
    fun changeShowingPreviewStatus(status: Boolean) {
        this.isShowingPreview.value = status
    }
    fun changeHighestDetected(detection: String) {
        this.highestDetected.value = detection
    }
}