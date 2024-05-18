package com.courses.cameraviewinorboai.np.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courses.cameraviewinorboai.np.repository.PictureRepository
import com.otaliastudios.cameraview.PictureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PictureViewModel @Inject constructor(
    private val repository: PictureRepository
): ViewModel() {

    private val _bitmap = MutableLiveData<Bitmap?>()
    val bitmap: LiveData<Bitmap?> get() = _bitmap

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _imageSaved = MutableLiveData<Boolean>()
    val imageSaved: LiveData<Boolean> get() = _imageSaved

    fun setPictureResult(result: PictureResult) {
        viewModelScope.launch {
            val resultBitmap = repository.getBitmap(result)
            resultBitmap.fold(
                onSuccess = { _bitmap.value = it },
                onFailure = {
                    _bitmap.value = null
                    _error.value = "Can't preview this format: " + result.format
                }
            )
        }
    }

    fun logPictureSize(result: PictureResult) {
        val sizeMessage = repository.logPictureSize(result)
        if (sizeMessage.isNotEmpty()) {
            Log.e("PicturePreview", sizeMessage)
        }
    }

    fun saveImage(fileName: String, bitmap: Bitmap) {
        viewModelScope.launch {
            _imageSaved.value = repository.saveImage(fileName, bitmap)
        }
    }
}