package com.courses.cameraviewinorboai.np.repository

import androidx.lifecycle.LifecycleOwner
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.controls.Preview
import com.otaliastudios.cameraview.filter.Filter
import java.io.File

class CameraRepository (
    private val cameraView: CameraView
)
{

    fun setLifeCycleOwner(lifecycleOwner: LifecycleOwner){
        cameraView.setLifecycleOwner(lifecycleOwner)
    }

    fun addCameraListener(listener: CameraListener){
        cameraView.addCameraListener(listener)
    }

    fun toggleCamera(): Facing{
        if(cameraView.isTakingPicture) return cameraView.facing
        return cameraView.toggleFacing()
    }

    fun getCameraMode(): Mode{
        return cameraView.mode
    }

    fun setFilter(filters: Filter){
        cameraView.filter = filters
    }

    fun getCameraPreview(): Preview{
        return cameraView.preview
    }

    fun capturePicture(){
        if(cameraView.isTakingPicture) return
        cameraView.takePicture()
    }

    fun captureSnapshot(){
        if(cameraView.isTakingPicture) return
        cameraView.takePictureSnapshot()
    }

    fun isTakingVideo() : Boolean{
      return cameraView.isTakingVideo
    }

    fun takeVideoSnapshot(file: File, duration:Int){
        cameraView.takeVideoSnapshot(file, duration)
    }

}