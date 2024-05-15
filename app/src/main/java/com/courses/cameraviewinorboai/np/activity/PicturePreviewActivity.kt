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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.courses.cameraviewinorboai.R
import com.courses.cameraviewinorboai.databinding.ActivityPicturePreviewBinding
import com.courses.cameraviewinorboai.np.other.Constants.REQUEST_CODE
import com.courses.cameraviewinorboai.np.other.ShareUtils
import com.google.android.material.appbar.MaterialToolbar
import com.otaliastudios.cameraview.PictureResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PicturePreviewActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPicturePreviewBinding.inflate(layoutInflater)
    }

    private var originalBitmap: Bitmap? = null

    companion object {
        var pictureResult: PictureResult? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val result = pictureResult ?: run {
            finish()
            return
        }
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)

        //todo set up the tooblar
        setSupportActionBar(toolbar)

        try {
            result.toBitmap(1000, 1000) { bitmap ->
                binding.previewImage.setImageBitmap(bitmap)
                originalBitmap = bitmap
            }
        } catch (e: UnsupportedOperationException) {
            binding.previewImage.setImageDrawable(ColorDrawable(Color.GREEN))
            Toast.makeText(this, "Can't preview this format: " + result.format, Toast.LENGTH_LONG)
                .show()
        }

        if (result.isSnapshot) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(result.data, 0, result.data.size, options)
            if (result.rotation % 180 != 0) {
                Log.e("PicturePreview", "The picture full size is ${result.size.height}x${result.size.width}")
            } else {
                Log.e("PicturePreview", "The picture full size is ${result.size.width}x${result.size.height}")
            }
        }


        binding.cropImage.setOnClickListener {
            val croppedBitmap = binding.previewImage.getCroppedBitmap()
            if (croppedBitmap != null) {
                binding.previewImage.setOriginalBitmap(croppedBitmap)
            } else {
                Toast.makeText(this, "Please select an area to crop", Toast.LENGTH_SHORT).show()
            }
        }

        binding.downloadImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
                saveImage()
            } else {
                askSelfPermission()
            }
        }
    }

    private fun askSelfPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    private fun saveImage() {
        originalBitmap?.let { bitmap ->
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToExternalStorage(fileName, bitmap)
            } else {
                saveImageToGalleryLegacy(fileName, bitmap)
            }
        }
    }

    private fun saveImageToExternalStorage(fileName: String, bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            try {
                val outputStream = contentResolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGalleryLegacy(fileName: String, bitmap: Bitmap) {
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString())
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null, null)
            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
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
}
