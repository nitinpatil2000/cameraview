package com.courses.cameraviewinorboai.np.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.otaliastudios.cameraview.PictureResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resumeWithException

class PictureRepository @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun getBitmap(result: PictureResult): Result<Bitmap?> = withContext(Dispatchers.IO) {
        return@withContext try {
            val bitmap = suspendCancellableCoroutine { continuation ->
                val mainHandler = Handler(Looper.getMainLooper())
                mainHandler.post {
                    result.toBitmap(1000, 1000) { bmp ->
                        if (continuation.isActive) {
                            continuation.resume(bmp) {
                                continuation.resumeWithException(it)
                            }
                        }
                    }
                }
            }
            Result.success(bitmap)
        } catch (e: UnsupportedOperationException) {
            Result.failure(e)
        }
    }


    fun logPictureSize(result: PictureResult): String {
        return if (result.isSnapshot) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(result.data, 0, result.data.size, options)
            if (result.rotation % 180 != 0) {
                "The picture full size is ${result.size.height}x${result.size.width}"
            } else {
                "The picture full size is ${result.size.width}x${result.size.height}"
            }
        } else {
            ""
        }
    }


    //todo save the image
    suspend fun saveImage(fileName:String, bitmap: Bitmap): Boolean =
        withContext(Dispatchers.IO){
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                return@withContext try {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        true
                    } ?: false
                } catch (e: Exception) {
                    false
                }
            } ?: false
        }
}