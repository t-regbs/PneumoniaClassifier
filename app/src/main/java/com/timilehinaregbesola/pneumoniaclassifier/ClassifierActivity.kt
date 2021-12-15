package com.timilehinaregbesola.pneumoniaclassifier

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.timilehinaregbesola.pneumoniaclassifier.databinding.ActivityClassifierBinding
import com.timilehinaregbesola.pneumoniaclassifier.ml.Pneumonia

class ClassifierActivity : AppCompatActivity() {

    private var bitmapp: List<Bitmap>? = null
    private var urii: List<Uri>? = null
    private var resultList: List<GalleryUploadViewModel.RecogResult>? = null
    private var currentResult: GalleryUploadViewModel.RecogResult? = null
    private var viewAdapter: RecognitionAdapter? = null

    private lateinit var binding: ActivityClassifierBinding
    private lateinit var viewModel: GalleryUploadViewModel

    var pageChangeCallback: com.github.islamkhsh.viewpager2.ViewPager2.OnPageChangeCallback = object : com.github.islamkhsh.viewpager2.ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            currentResult = resultList?.get(position)
            viewAdapter?.submitList(currentResult?.recogList)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassifierBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.makeTransparentStatusBar()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
        }

        setupToolBar()

        viewModel = ViewModelProviders.of(this)[GalleryUploadViewModel::class.java]
        urii = intent.extras?.get("image-uri") as List<Uri>?
        println("uri: $urii")
        if (urii != null) {
            bitmapp = uriListToBitmapList(urii)
            binding.viewPager.apply {
                adapter = CarouselAdapter(bitmapp as ArrayList<Bitmap>)
                registerOnPageChangeCallback(pageChangeCallback)
            }
//            binding..setImageBitmap(bitmapp)
            binding.btnRun.isEnabled = true
        }
        viewAdapter = RecognitionAdapter(this)
        binding.recognitionResults.adapter = viewAdapter

        binding.recognitionResults.itemAnimator = null
        viewModel.recognitionList.observe(
            this
        ) {
            // update UI
            println("Result: $it")
            binding.btnRun.isEnabled = true
            binding.loading.visibility = View.GONE
            resultList = it
            currentResult = resultList?.get(binding.viewPager.currentItem)
            viewAdapter?.submitList(currentResult?.recogList)
        }

        binding.btnRun.setOnClickListener {
            processImage()
        }
    }

    private fun setupToolBar() {
        val backArrow = ContextCompat.getDrawable(this, R.drawable.ic_baseline_arrow_back_24)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(backArrow)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "Pneumonia Classifier"
        binding.appBarLayout.bringToFront()
    }

    fun processImage() {
        binding.btnRun.isEnabled = false
        binding.loading.visibility = View.VISIBLE
        val cxrModel = Pneumonia.newInstance(this)
        viewModel.process(cxrModel, bitmapp!!)
    }

    private fun Window.makeTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
    }

    private fun uriToBitmap(uri: Uri?): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun uriListToBitmapList(uris: List<Uri>?): List<Bitmap> {
        val list = mutableListOf<Bitmap>()
        if (uris != null) {
            for (uri in uris) {
                list.add(uriToBitmap(uri))
            }
        }
        return list
    }
}
