package com.courses.cameraviewinorboai.np.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.courses.cameraviewinorboai.R
import com.courses.cameraviewinorboai.databinding.ActivityMainBinding
import com.courses.cameraviewinorboai.np.repository.CameraRepository
import com.courses.cameraviewinorboai.np.viewmodel.CameraViewModel
import com.courses.cameraviewinorboai.np.factory.ViewModelFactory
import com.otaliastudios.cameraview.CameraException
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var repository: CameraRepository
    private val viewModel by viewModels<CameraViewModel>{
        ViewModelFactory(repository)
    }

    companion object {
        val LOG = CameraLogger.create("CameraViewAppInOrboAI")!!
    }

    private var captureTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //TODO Initialize the repository
        repository = CameraRepository(binding.cameraView)

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)

        //todo attached cameraView to the lifecycle
        viewModel.setLifecycleOwner(this)
        viewModel.addCameraListener(Listener())

        observeLiveData()

        //todo handled the click listener
        binding.toggleCamera.setOnClickListener(this)
        binding.changeFilter.setOnClickListener(this)
        binding.capturePicture.setOnClickListener(this)
        binding.capturePictureSnapshot.setOnClickListener(this)
        binding.captureVideoSnapshot.setOnClickListener(this)

        //todo for the watermark animation
        val animator = ValueAnimator.ofFloat(1f, 0.8f)
        animator.duration = 300
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float

            binding.watermark.apply {
                scaleX = scale
                scaleY = scale
                rotation += 2
            }
        }
        animator.start()
    }

    private fun observeLiveData(){
        viewModel.message.observe(this){
            message(it, false)
        }

        viewModel.filter.observe(this){ filter ->
            binding.cameraView.filter = filter
        }

        viewModel.pictureTaken.observe(this){ result ->
            PicturePreviewActivity.pictureResult = result
            startActivity(Intent(this, PicturePreviewActivity::class.java))
            LOG.w("onPictureTaken called! Launched activity.")
        }

        viewModel.videoTaken.observe(this){ result ->
            VideoPreviewActivity.videoResult = result
            startActivity(Intent(this, VideoPreviewActivity::class.java))
            LOG.w("onVideoTaken called! Launching activity.")
        }
    }

    //todo handled the view click and call the viewModel function
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.toggleCamera -> viewModel.toggleCamera()
            R.id.changeFilter -> viewModel.changeFilter()
            R.id.capturePicture -> viewModel.capturePicture()
            R.id.capturePictureSnapshot -> viewModel.capturePictureSnapShot()
            R.id.captureVideoSnapshot -> viewModel.captureVideoSnapShot(filesDir)
        }
    }


    //todo for showing the msg on the screen
    private fun message(content: String, important: Boolean) {
        if (important) {
            LOG.w(content)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        } else {
            LOG.i(content)
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }


    //todo for handled the camera events
    inner class Listener : CameraListener() {
        override fun onCameraError(exception: CameraException) {
            message("Got CameraException #" + exception.reason, true)
        }

        override fun onZoomChanged(
            newValue: Float,
            bounds: FloatArray,
            fingers: Array<out PointF>?
        ) {
            super.onZoomChanged(newValue, bounds, fingers)
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            if (binding.cameraView.isTakingVideo) {
                message("Captured while taking video. Size=" + result.size, false)
                return
            }

            viewModel.onPictureTaken(result)
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            viewModel.onVideoTaken(result)
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            LOG.w("onVideoRecordingStart!")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            message("Video taken. Processing...", false)
            LOG.w("onVideoRecordingEnd!")
        }

    }

    //todo for the permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val valid = grantResults.all {
            it == PackageManager.PERMISSION_GRANTED
        }
        if (valid && !binding.cameraView.isOpened) {
            binding.cameraView.open()
        }
    }
}









