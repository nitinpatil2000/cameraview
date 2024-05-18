package com.courses.cameraviewinorboai.np.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect as OpenCVRect


class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint()
    private var cropRect: android.graphics.Rect? = null
    private var originalBitmap: Bitmap? = null
    private var isCropEnabled = false
    var hasCropped = false // Flag to check if cropping is done


    init {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the crop rectangle if cropping is enabled
        if (isCropEnabled) {
            cropRect?.let {
                canvas.drawRect(it, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isCropEnabled && !hasCropped) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start the rectangle
                    cropRect = android.graphics.Rect(
                        event.x.toInt(),
                        event.y.toInt(),
                        event.x.toInt(),
                        event.y.toInt()
                    )
                    invalidate()
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    // Update the rectangle size
                    cropRect?.apply {
                        right = event.x.toInt()
                        bottom = event.y.toInt()
                    }
                    invalidate()
                    return true
                }

                MotionEvent.ACTION_UP -> {
                    // Finalize the rectangle
                    cropRect?.apply {
                        right = event.x.toInt()
                        bottom = event.y.toInt()
                    }
                    val croppedBitmap = getCroppedBitmapUsingOpenCV()
                    if (croppedBitmap != null) {
                        setOriginalBitmap(croppedBitmap)
                        hasCropped = true
                        cropRect = null
                        isCropEnabled = false
                        invalidate()
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
    fun setOriginalBitmap(bitmap: Bitmap) {
        originalBitmap = bitmap
        setImageBitmap(bitmap)
    }

    private fun getCroppedBitmapUsingOpenCV(): Bitmap? {
        return originalBitmap?.let { bitmap ->
            cropRect?.let { rect ->
                // Ensure the rect is within the bitmap bounds
                val left = rect.left.coerceIn(0, bitmap.width)
                val top = rect.top.coerceIn(0, bitmap.height)
                val right = rect.right.coerceIn(0, bitmap.width)
                val bottom = rect.bottom.coerceIn(0, bitmap.height)
                val width = (right - left).coerceAtLeast(1)
                val height = (bottom - top).coerceAtLeast(1)

                // Convert Bitmap to OpenCV Mat
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)

                // Create the cropping rectangle for OpenCV
                val cropRect = OpenCVRect(left, top, width, height)

                // Crop the image using OpenCV
                val croppedMat = Mat(mat, cropRect)

                // Convert the cropped Mat back to Bitmap
                val croppedBitmap = Bitmap.createBitmap(
                    croppedMat.cols(),
                    croppedMat.rows(),
                    Bitmap.Config.ARGB_8888
                )
                Utils.matToBitmap(croppedMat, croppedBitmap)

                croppedBitmap
            }
        }
    }

    fun setIsCropEnabled(enable: Boolean) {
        if(!hasCropped){
            isCropEnabled = enable
            invalidate()
        }

    }
}