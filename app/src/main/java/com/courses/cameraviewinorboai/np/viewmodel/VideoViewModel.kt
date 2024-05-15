package com.courses.cameraviewinorboai.np.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courses.cameraviewinorboai.np.repository.VideoRepository
import com.otaliastudios.cameraview.VideoResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val repository: VideoRepository
): ViewModel() {

    private val _videoDetails = MutableLiveData<VideoRepository.VideoDetails>()
    val videoDetails:LiveData<VideoRepository.VideoDetails> = _videoDetails

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun fetchVideoDetails(result:VideoResult){
        viewModelScope.launch {
            try {
                val details = repository.getVideoDetails(result)
                _videoDetails.value = details
            }catch (e: Exception){
                _message.value = "Failed To Fetch the video details"
            }
        }
    }


}

