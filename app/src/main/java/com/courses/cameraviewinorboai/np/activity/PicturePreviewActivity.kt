package com.courses.cameraviewinorboai.np.activity

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.courses.cameraviewinorboai.R
import com.courses.cameraviewinorboai.databinding.ActivityPicturePreviewBinding
import com.courses.cameraviewinorboai.np.other.Constants.REQUEST_CODE
import com.courses.cameraviewinorboai.np.other.ShareUtils
import com.courses.cameraviewinorboai.np.viewmodel.PictureViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.otaliastudios.cameraview.PictureResult
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class PicturePreviewActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPicturePreviewBinding.inflate(layoutInflater)
    }
    private val viewModel by viewModels<PictureViewModel>()

    //todo for save the original bitmap
    private var originalBitmap: Bitmap? = null

    companion object {
        var pictureResult: PictureResult? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //TODO Load OpenCV library
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Initialization failed!")
        } else {
            Log.d("OpenCV", "Initialization succeeded!")
        }

        val result = pictureResult ?: run {
            finish()
            return
        }

        //todo set up the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Todo call this viewModel method
        viewModel.setPictureResult(result)
        viewModel.logPictureSize(result)
        viewModel.setPictureResult(result)

        observeViewModelVariables()

        handledClickEvent()

    }

    private fun observeViewModelVariables() {
        viewModel.bitmap.observe(this) { bitmap ->
            bitmap?.let {
                binding.previewImage.setOriginalBitmap(it)
                originalBitmap = it
            } ?: run {
                binding.previewImage.setImageDrawable(ColorDrawable(Color.GREEN))
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.imageSaved.observe(this) { isImageSaved ->
            if (isImageSaved) {
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handledClickEvent() {
        binding.cropImage.setOnClickListener {
            if(!binding.previewImage.hasCropped){
                binding.previewImage.setIsCropEnabled(true)
                Toast.makeText(this, "Crop the image", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(this, "Image has already been cropped", Toast.LENGTH_SHORT).show()
            }
        }

        binding.downloadImage.setOnClickListener {
            checkPermissionAndSaveImage()
        }
    }


    private fun checkPermissionAndSaveImage() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
            saveImage()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun saveImage() {
        originalBitmap?.let { bitmap ->
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            viewModel.saveImage(fileName, bitmap)
        }   
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                saveImage()
            }
            else {
                Toast.makeText(this, "Please provide the required permission !!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pictureResult = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                pictureResult?.let { ShareUtils.sharePicture(this, it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                Toast.makeText(this, "Please provide the required permission !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
