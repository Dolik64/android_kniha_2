package com.example.kniha_20.ui.screens.editor

import androidx.lifecycle.ViewModel
import com.example.kniha_20.data.repository.AlbumRepository
import com.example.kniha_20.data.utils.updateImageInTree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditorViewModel : ViewModel() {
    // Data pro UI
    private val _pageData = MutableStateFlow(AlbumRepository.loadDefaultAlbum())
    val pageData = _pageData.asStateFlow()

    private var activeImageId: String? = null

    fun onImageClicked(id: String) {
        activeImageId = id
    }

    fun onImageSelected(uri: String) {
        activeImageId?.let { id ->
            _pageData.update { currentData ->
                updateImageInTree(currentData, id, uri)
            }
            activeImageId = null
        }
    }
}