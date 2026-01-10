package com.example.kniha_20.data.repository

import com.example.kniha_20.data.model.BookComponent
import com.example.kniha_20.data.model.MockData

object AlbumRepository {
    // Zatím vrací jen mock data, později bude číst JSON
    fun loadDefaultAlbum(): BookComponent {
        return MockData.getTestAlbum()
    }
}