package com.bangkit.auris.customview

import com.bangkit.auris.tflite.Classifier.Recognition

interface ResultsView {
    fun setResults(results: List<Recognition>)
}
