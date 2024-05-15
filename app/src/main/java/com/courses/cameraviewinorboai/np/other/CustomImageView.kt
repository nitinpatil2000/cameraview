package com.courses.cameraviewinorboai.np.other

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint: Paint = Paint()
    var cropRect: Rect? = null
    private var originalBitmap: Bitmap? = null

    init {
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f // Adjust the stroke width as needed
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw the crop rectangle if it exists
        cropRect?.let { canvas.drawRect(it, paint) }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Start drawing the crop rectangle
                cropRect = Rect(event.x.toInt(), event.y.toInt(), event.x.toInt(), event.y.toInt())
                invalidate() // Redraw the view
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Update the crop rectangle as the user moves their finger
                cropRect?.apply {
                    right = event.x.toInt()
                    bottom = event.y.toInt()
                }
                invalidate() // Redraw the view
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Finalize the crop rectangle when the user lifts their finger
                cropRect?.apply {
                    right = event.x.toInt()
                    bottom = event.y.toInt()
                }
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // Method to set the original bitmap
    fun setOriginalBitmap(bitmap: Bitmap) {
        originalBitmap = bitmap
        setImageBitmap(bitmap)
    }

    // Method to get the cropped bitmap
    fun getCroppedBitmap(): Bitmap? {
        return originalBitmap?.let { bitmap ->
            cropRect?.let { rect ->
                // Ensure rect is within bitmap bounds
                val left = rect.left.coerceIn(0, bitmap.width)
                val top = rect.top.coerceIn(0, bitmap.height)
                val width = rect.width().coerceIn(0, bitmap.width - left)
                val height = rect.height().coerceIn(0, bitmap.height - top)
                Bitmap.createBitmap(bitmap, left, top, width, height)
            }
        }
    }
}

