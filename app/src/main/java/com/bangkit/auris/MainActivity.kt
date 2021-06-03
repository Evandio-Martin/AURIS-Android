package com.bangkit.auris

import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.Paint.Style
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bangkit.auris.customview.OverlayView
import com.bangkit.auris.customview.OverlayView.DrawCallback
import com.bangkit.auris.env.BorderedText
import com.bangkit.auris.env.ImageUtils
import com.bangkit.auris.env.Logger
import com.bangkit.auris.fragment.DictionaryFragment
import com.bangkit.auris.fragment.HomeFragment
import com.bangkit.auris.tflite.Classifier
import com.bangkit.auris.tflite.TFLiteObjectDetectionAPIModel
import com.bangkit.auris.tracking.MultiBoxTracker
import com.bangkit.auris.viewmodel.PageViewModel
import com.bangkit.auris.viewmodel.ViewModelFactory
import com.bangkit.auris.R
import com.bangkit.auris.databinding.ActivityMainBinding
import java.io.IOException
import java.util.*

class MainActivity : CameraActivity(), OnImageAvailableListener {
    // Binding
    private lateinit var binding: ActivityMainBinding
    private lateinit var activeFragment: Fragment
    //viewModel
    private lateinit var pageViewModel: PageViewModel
    //fragments
    private lateinit var homeFragment: HomeFragment
    private lateinit var dictionaryFragment: DictionaryFragment

    private lateinit var trackingOverlay: OverlayView
    private var sensorOrientation: Int? = null
    private var detector: Classifier? = null
    private var lastProcessingTimeMs: Long = 0
    private var rgbFrameBitmap: Bitmap? = null
    private var croppedBitmap: Bitmap? = null
    private var cropCopyBitmap: Bitmap? = null
    private var computingDetection = false
    private var timestamp: Long = 0
    private var frameToCropTransform: Matrix? = null
    private var cropToFrameTransform: Matrix? = null
    private var tracker: MultiBoxTracker? = null
    private var borderedText: BorderedText? = null

    override val layoutId: Int
        get() = R.layout.camera_connection_fragment_tracking

    override val desiredPreviewFrameSize: Size
        get() = DESIRED_PREVIEW_SIZE

    override fun onCreate(savedInstanceState: Bundle?) {
        LOGGER.d("onCreate $this")
        //Instantiate ViewModel with ViewModelFactory
        this.pageViewModel = ViewModelFactory().createViewModel(this, application, PageViewModel::class.java)

        super.onCreate(null)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(this.binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //Initiate Fragments
        this.homeFragment = HomeFragment.newInstance()
        this.dictionaryFragment = DictionaryFragment.newInstance()
        this.activeFragment = this.homeFragment
        this.initiateFragment(this.homeFragment, "HOME")
        this.initiateFragment(this.dictionaryFragment, "DICTIONARY")

        //Create Custom ToolBar
        this.setSupportActionBar(this.binding.toolbar)
        this.supportActionBar?.setDisplayShowTitleEnabled(false)

        //Observe Page state
        this.pageViewModel.getPage().observe(this, {
            this.changePage(it)
        })

        //Onclick listener for bottomNavigation
        this.binding.navView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    this.pageViewModel.changePage("HOME")
//                    this.changePage("HOME")
                    true
                }
                R.id.navigation_dictionary -> {
                    this.pageViewModel.changePage("DICTIONARY")
//                    this.changePage("DICTIONARY")
                    true
                }
                else -> false
            }
        }

        if (this.hasPermission()) {
            this.setFragment()
        } else {
            this.requestPermission()
        }
    }

    private fun initiateFragment(frag: Fragment, tag: String) {
        val manager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = manager.beginTransaction()

        ft.add(this.binding.fragmentContainer.id, frag, tag).hide(frag).commit()
    }

    private fun changePage(pageName: String): Boolean {
        val manager: FragmentManager = supportFragmentManager
        val ft: FragmentTransaction = manager.beginTransaction()

        when(pageName) {
            "HOME" -> {
                ft.hide(activeFragment).show(this.homeFragment).commit()
//                this.pageViewModel.changeProcessingStatus(true)
                this.activeFragment = this.homeFragment
                this.changeTitle("Home")
            }
            "DICTIONARY" -> {
                ft.hide(activeFragment).show(this.dictionaryFragment).commit()
                this.pageViewModel.changeProcessingStatus(false)
                this.activeFragment = this.dictionaryFragment
                this.changeTitle("Dictionary")
            }
        }

        return true
    }

    private fun changeTitle(title: String){
        this.binding.toolbar.title = title
    }

    override fun onPreviewSizeChosen(size: Size, cameraRotation: Int) {
        val textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, resources.displayMetrics)
        borderedText = BorderedText(textSizePx)
        borderedText!!.setTypeface(Typeface.MONOSPACE)

        tracker = MultiBoxTracker(this)

        var cropSize = TF_OD_API_INPUT_SIZE

        try {
            detector = TFLiteObjectDetectionAPIModel.create(
                    assets,
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
            )
            cropSize = TF_OD_API_INPUT_SIZE
        } catch (e: IOException) {
            e.printStackTrace()
            LOGGER.e(e, "Exception initializing classifier!")
            val toast = Toast.makeText(
                    applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT)
            toast.show()
            finish()
        }
        previewWidth = size.width
        previewHeight = size.height

        sensorOrientation = cameraRotation - screenOrientation

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight)
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888)
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888)

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                cropSize, cropSize,
                sensorOrientation!!, MAINTAIN_ASPECT
        )

        cropToFrameTransform = Matrix()
        frameToCropTransform!!.invert(cropToFrameTransform)

        trackingOverlay = findViewById<View>(R.id.tracking_overlay) as OverlayView
        trackingOverlay.addCallback(
                object : DrawCallback {
                    override fun drawCallback(canvas: Canvas) {
                        tracker!!.draw(canvas)
                        if (isDebug) {
                            tracker!!.drawDebug(canvas)
                        }
                    }
                })

        tracker!!.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation!!)
    }

    override fun processImage() {
        ++timestamp
        val currTimestamp = timestamp
        trackingOverlay.postInvalidate()

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage()
            return
        }

        computingDetection = true
        LOGGER.i("Preparing image $currTimestamp for detection in bg thread.")

        rgbFrameBitmap!!.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight)

        readyForNextImage()

        val canvas = Canvas(croppedBitmap!!)
        canvas.drawBitmap(rgbFrameBitmap!!, frameToCropTransform!!, null)
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap!!)
        }

        runInBackground(
            Runnable {
                LOGGER.i("Running detection on image $currTimestamp")
                val startTime = SystemClock.uptimeMillis()
                val results = detector!!.recognizeImage(croppedBitmap!!)
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap!!)
                val canvas = Canvas(cropCopyBitmap!!)
                val paint = Paint()
                paint.color = Color.RED
                paint.style = Style.STROKE
                paint.strokeWidth = 2.0f

                var minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
                when (MODE) {
                    DetectorMode.TF_OD_API -> minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API
                }

                val mappedRecognitions = LinkedList<Classifier.Recognition>()
                var max = 0f
                var maxDetected = ""

                for (result in results) {
                    val location = result.location
                    if (result.confidence >= minimumConfidence) {
                        if (result.confidence > max) {
                            max = result.confidence
                            maxDetected = result.title!!
                        }

                        canvas.drawRect(location, paint)

                        cropToFrameTransform!!.mapRect(location)

                        result.location = location
                        mappedRecognitions.add(result)
                    }
                }

                if (maxDetected != "") {
                    this.pageViewModel.highestDetected.postValue(maxDetected)
                }

                tracker!!.trackResults(mappedRecognitions, currTimestamp)
                trackingOverlay.postInvalidate()

                computingDetection = false
            })
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum class DetectorMode {
        TF_OD_API
    }

    override fun setUseNNAPI(isChecked: Boolean) {
        runInBackground(Runnable { detector!!.setUseNNAPI(isChecked) })
    }

    override fun setNumThreads(numThreads: Int) {
        runInBackground(Runnable { detector!!.setNumThreads(numThreads) })
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onClick(p0: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private val LOGGER = Logger()
        // Configuration values for the prepackaged SSD model.
        private const val TF_OD_API_INPUT_SIZE = 320
        private const val TF_OD_API_IS_QUANTIZED = false
        private const val TF_OD_API_MODEL_FILE = "detect.tflite"
        private const val TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt"
        private val MODE = DetectorMode.TF_OD_API
        // Minimum detection confidence to track a detection.
        private const val MINIMUM_CONFIDENCE_TF_OD_API = 0.75f
        private const val MAINTAIN_ASPECT = false
        private val DESIRED_PREVIEW_SIZE = Size(640, 480)
        private const val SAVE_PREVIEW_BITMAP = false
        private const val TEXT_SIZE_DIP = 10f
    }
}
