package com.timilehinaregbesola.pneumoniaclassifier

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timilehinaregbesola.pneumoniaclassifier.ml.Pneumonia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GalleryUploadViewModel : ViewModel() {
    private val _recognitionList = MutableLiveData<List<RecogResult>>()

    val recognitionList: LiveData<List<RecogResult>> = _recognitionList
    fun process(model: Pneumonia, bitmap: List<Bitmap>) {
        viewModelScope.launch {
            processImages(model, bitmap)
        }
    }

    data class Recognition(val label: String, val confidence: Float) {

        // For easy logging
        override fun toString(): String {
            return "$label / $probabilityString"
        }

        // Output probability as a string to enable easy data binding
        val probabilityString = String.format("%.1f%%", confidence * 100.0f)
    }

    data class RecogResult(val id: Int, val recogList: List<Recognition>)

    private suspend fun processImages(model: Pneumonia, bitmaps: List<Bitmap>) {
        println("start")
        withContext(Dispatchers.IO) {
            val resultList = mutableListOf<RecogResult>()
            for (bitmap in bitmaps) {
                val items = mutableListOf<Recognition>()
                val resized = Bitmap.createScaledBitmap(bitmap, 320, 320, true)
                val byteBuffer = convertBitmapToByteBuffer(resized)
                val tfBuffer =
                    TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)
                tfBuffer.loadBuffer(byteBuffer)
                val outputs = model.process(tfBuffer)
                val output1 = outputs.outputFeature0AsTensorBuffer.getFloatValue(0)
                val output2 = outputs.outputFeature0AsTensorBuffer.getFloatValue(1)
                val output3 = outputs.outputFeature0AsTensorBuffer.getFloatValue(2)

                // Converting the top probability items into a list of recognitions
                items.add(Recognition("BACTERIAL", output1))
                items.add(Recognition("VIRAL", output3))
                items.add(Recognition("CLEAR", output2))
                items.sortByDescending { it.confidence }
                println("end")
                val result = RecogResult(id = bitmaps.indexOf(bitmap), recogList = items)
                resultList.add(result)
            }
            _recognitionList.postValue(resultList)
            model.close()
        }
    }
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(320 * 320 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(320 * 320)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until 320) {
            for (j in 0 until 320) {
                val pixelVal = pixels[pixel++]

                val IMAGE_MEAN = 127.5f
                val IMAGE_STD = 127.5f
                byteBuffer.putFloat(((pixelVal shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((pixelVal shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((pixelVal and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        bitmap.recycle()

        return byteBuffer
    }
}
