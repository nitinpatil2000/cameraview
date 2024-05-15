package com.courses.cameraviewinorboai.np.repository

import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.size.AspectRatio
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor() {

    suspend fun getVideoDetails(result: VideoResult): VideoDetails {
        //TODO Simulate a delay for data fetching
        delay(100)
        val ratio = AspectRatio.of(result.size)
        return VideoDetails(result, ratio)
    }

    data class VideoDetails(
        val result: VideoResult,
        val ratio: AspectRatio
    )

}