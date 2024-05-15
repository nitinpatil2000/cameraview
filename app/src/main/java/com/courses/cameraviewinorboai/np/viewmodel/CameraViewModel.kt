package com.courses.cameraviewinorboai.np.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.courses.cameraviewinorboai.np.repository.CameraRepository
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filter
import com.otaliastudios.cameraview.filter.Filters
import java.io.File

class CameraViewModel(
    private val repository: CameraRepository
) : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _filter = MutableLiveData<Filter>()
    val filter: LiveData<Filter> = _filter

    private val _pictureTaken = MutableLiveData<PictureResult>()
    val pictureTaken: LiveData<PictureResult> = _pictureTaken

    private var currentFilter = 0
    private var allFilters = Filters.entries.toTypedArray()

    private val _videoTaken = MutableLiveData<VideoResult>()
    val videoTaken: LiveData<VideoResult> = _videoTaken

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        repository.setLifeCycleOwner(lifecycleOwner)
    }

    fun addCameraListener(listener: CameraListener) {
        repository.addCameraListener(listener)
    }

    fun toggleCamera() {
        when (repository.toggleCamera()) {
            Facing.BACK -> _message.value = "Switched to back camera!"
            Facing.FRONT -> _message.value = "Switched to front camera!"
        }
    }

    fun changeFilter() {
        if (repository.getCameraPreview() != Preview.GL_SURFACE) {
            _message.value = "Filters are supported only when preview is Preview.GL_SURFACE."
            return
        }

        if (currentFilter < allFilters.size - 1) {
            currentFilter++
        } else {
            currentFilter = 0
        }

        val filter = allFilters[currentFilter]
        _filter.value = filter.newInstance()
        repository.setFilter(filter.newInstance())
    }


    fun capturePicture() {
        if (repository.getCameraMode() == Mode.VIDEO) {
            _message.value = "Can't take HQ pictures while in VIDEO mode."
            return
        }
        _message.value = "Capturing picture..."
        repository.capturePicture()
    }

    //todo used in the cameraViewListener
    fun onPictureTaken(result: PictureResult) {
        _pictureTaken.value = result
    }

    fun capturePictureSnapShot() {
        if (repository.getCameraPreview() != Preview.GL_SURFACE) {
            _message.value = "Picture snapshots are only allowed with the GL_SURFACE preview."
            return
        }
        _message.value = "Capturing picture snapshot..."
        repository.captureSnapshot()
    }

    fun captureVideoSnapShot(filesDir: File) {
        if (repository.isTakingVideo()) {
            _message.value = "Already taking video."
            return
        }

        if (repository.getCameraPreview() != Preview.GL_SURFACE) {
            _message.value = "Video snapshots are only allowed with the GL_SURFACE preview."
            return
        }

        _message.value = "Recording snapshot for 5 seconds..."
        repository.takeVideoSnapshot(File(filesDir, "video.mp4"), 5000)
    }

    fun onVideoTaken(result: VideoResult) {
        _videoTaken.value = result
    }

}