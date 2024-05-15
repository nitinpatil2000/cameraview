package com.courses.cameraviewinorboai.np.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.courses.cameraviewinorboai.R
import com.courses.cameraviewinorboai.databinding.ActivityVideoPreviewBinding
import com.courses.cameraviewinorboai.np.other.ShareUtils
import com.courses.cameraviewinorboai.np.viewmodel.VideoViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.otaliastudios.cameraview.VideoResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPreviewActivity : AppCompatActivity() {
    companion object {
        var videoResult: VideoResult? = null
    }

    private val binding by lazy {
        ActivityVideoPreviewBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<VideoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Preview Video"


        val result = videoResult ?: run {
            finish()
            return
        }

        viewModel.fetchVideoDetails(result)

        //todo observe the live data
        observeTheLiveData()


    }

    private fun observeTheLiveData() {
        viewModel.videoDetails.observe(this) { details ->
            binding.apply {
                video.setOnClickListener { playVideo() }
                actualResolution.setTitleAndMessage(
                    "Size",
                    "${details.result.size} (${details.ratio})"
                )
                isSnapshot.setTitleAndMessage("Snapshot", details.result.isSnapshot.toString())
                rotation.setTitleAndMessage("Rotation", details.result.rotation.toString())
                audio.setTitleAndMessage("Audio", details.result.audio.name)
                audioBitRate.setTitleAndMessage(
                    "Audio bit rate",
                    "${details.result.audioBitRate} bits per sec."
                )
                videoCodec.setTitleAndMessage("VideoCodec", details.result.videoCodec.name)
                audioCodec.setTitleAndMessage("AudioCodec", details.result.audioCodec.name)
                videoBitRate.setTitleAndMessage(
                    "Video bit rate",
                    "${details.result.videoBitRate} bits per sec."
                )
                videoFrameRate.setTitleAndMessage(
                    "Video frame rate",
                    "${details.result.videoFrameRate} fps"
                )

                // Video setup
                setupVideo(details.result)

            }
        }

        viewModel.message.observe(this){ message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVideo(result: VideoResult) {
        val controller = MediaController(this)
        controller.setAnchorView(binding.video)
        controller.setMediaPlayer(binding.video)

        binding.video.apply {
            setMediaController(controller)
            setVideoURI(Uri.fromFile(result.file))

            setOnPreparedListener { mp ->
                val lp = binding.video.layoutParams
                val videoWidth = mp.videoWidth.toFloat()
                val videoHeight = mp.videoHeight.toFloat()
                val viewWidth = binding.video.width.toFloat()
                lp.height = (viewWidth * (videoHeight / videoWidth)).toInt()
                layoutParams = lp
                playVideo()

                if (result.isSnapshot) {
                    Log.e("VideoPreview", "The video full size is $videoWidth x $videoHeight")
                }
            }
        }
    }

    private fun playVideo() {
        binding.video.apply {
            if (!isPlaying) {
                start()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            videoResult = null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.share, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                videoResult?.let { ShareUtils.shareVideo(this, it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}