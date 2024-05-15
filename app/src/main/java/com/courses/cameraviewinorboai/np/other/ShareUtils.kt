package com.courses.cameraviewinorboai.np.other

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.courses.cameraviewinorboai.np.activity.PicturePreviewActivity
import com.otaliastudios.cameraview.CameraUtils
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.PictureFormat
import java.io.File

object ShareUtils {
    fun sharePicture(context: Context, pictureResult: PictureResult) {
        Toast.makeText(context, "Sharing...", Toast.LENGTH_SHORT).show()
        val extension = when (requireNotNull(PicturePreviewActivity.pictureResult).format) {
            PictureFormat.JPEG -> "jpg"
            PictureFormat.DNG -> "dng"
            else -> throw RuntimeException("Unknown format.")
        }
        val destFile = File(context.filesDir, "picture.$extension")
        CameraUtils.writeToFile(
            requireNotNull(PicturePreviewActivity.pictureResult?.data),
            destFile
        ) { file ->
            if (file != null) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*"
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider", file
                )

                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Error while writing file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    fun shareVideo(context: Context, videoResult: VideoResult){
        Toast.makeText(context, "Sharing...", Toast.LENGTH_SHORT).show()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "video/*"
        val uri = FileProvider.getUriForFile(context,
            context.packageName + ".provider",
            videoResult!!.file)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)

    }
}
