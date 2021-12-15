package com.timilehinaregbesola.pneumoniaclassifier

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.timilehinaregbesola.pneumoniaclassifier.databinding.ActivityMainBinding
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val PICK_IMAGES = 1
        const val REQUEST_IMAGE_CAPTURE = 2
        const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityMainBinding
    lateinit var currentPhotoPath: String
    private var camUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.makeTransparentStatusBar()
        window.statusBarColor = getColor(R.color.white)

        binding.btnUpload.setOnClickListener { pickImage() }
        binding.btnPhoto.setOnClickListener { takePhoto() }
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

    private fun takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                // Ensure that there's a camera activity to handle the intent
                takePictureIntent.resolveActivity(packageManager)?.also {
                    // Create the File where the photo should go
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        // Error occurred while creating the File
                        Toast.makeText(this, "There was an error creating image file", Toast.LENGTH_SHORT).show()
                        null
                    }
                    // Continue only if the File was successfully created
                    photoFile?.also {
                        val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "com.timilehinaregbesola.pneumoniaclassifier.fileprovider",
                            it
                        )
                        camUri = photoURI
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                112
            )
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun pickImage() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = "image/*"
            getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = "image/*"
            pickIntent.putExtra("crop", "true")
            pickIntent.putExtra("scale", true)
            pickIntent.putExtra("outputX", 224)
            pickIntent.putExtra("outputY", 224)
            pickIntent.putExtra("aspectX", 1)
            pickIntent.putExtra("aspectY", 1)
            pickIntent.putExtra("return-data", true)

            val chooserIntent = Intent.createChooser(getIntent, "Select Images")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            startActivityForResult(chooserIntent, PICK_IMAGES)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES) {
            if (resultCode != RESULT_OK || data == null) {
                Timber.e("Image uri: Result not ok")
                return
            }
            val uris = getClipDataUris(data)
            if (uris != null) {
                Timber.d("image uris: $uris")
                val intent = Intent(this, ClassifierActivity::class.java)
                intent.putExtra("image-uri", uris as Serializable)
                startActivity(intent)
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode != RESULT_OK || data == null) {
                Timber.e("Image uri: Result not ok")
                return
            }
//            val uris = getClipDataUris(data)
            val uris = listOf(camUri)
            Timber.d("image uris: $uris")
            val intent = Intent(this, ClassifierActivity::class.java)
            intent.putExtra("image-uri", uris as Serializable)
            startActivity(intent)
        }
    }

    private fun getClipDataUris(intent: Intent): List<Uri>? {
        // Use a LinkedHashSet to maintain any ordering that may be
        // present in the ClipData
        val resultSet = LinkedHashSet<Uri>()
        if (intent.data != null) {
            resultSet.add(intent.data!!)
        }
        val clipData = intent.clipData
        if (clipData == null && resultSet.isEmpty()) {
            return emptyList<Uri>()
        } else if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                if (uri != null) {
                    resultSet.add(uri)
                }
            }
        }
        return ArrayList(resultSet)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // pick image after request permission success
                    pickImage()
                }
            }
            112 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // pick image after request permission success
                    takePhoto()
                }
            }
        }
    }
}
