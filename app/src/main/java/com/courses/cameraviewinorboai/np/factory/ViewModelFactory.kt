package com.courses.cameraviewinorboai.np.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.courses.cameraviewinorboai.np.repository.CameraRepository
import com.courses.cameraviewinorboai.np.viewmodel.CameraViewModel

class ViewModelFactory(private val repository: CameraRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}