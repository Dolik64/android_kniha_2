package com.example.kniha_20.data.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.kniha_20.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Funkce pro aktualizaci stromu (přesunuto z MainActivity)
fun updateImageInTree(component: BookComponent, targetId: String, newUrl: String): BookComponent {
    return when (component) {
        is ImagerThing -> {
            if (component.id == targetId) {
                component.copy(options = component.options?.copy(url = newUrl) ?: ImageOptions(url = newUrl))
            } else {
                component
            }
        }
        is BackgroundDecorator -> component.copy(
            slots = component.slots?.let { it.copy(content = updateImageInTree(it.content, targetId, newUrl)) }
        )
        is InsetDecorator -> component.copy(
            slots = component.slots?.let { it.copy(content = updateImageInTree(it.content, targetId, newUrl)) }
        )
        is GridLayout -> component.copy(
            slots = component.slots.map { updateImageInTree(it, targetId, newUrl) }
        )
        is SplitLayout -> component.copy(
            slots = component.slots?.let {
                SplitSlots(
                    first = updateImageInTree(it.first, targetId, newUrl),
                    second = updateImageInTree(it.second, targetId, newUrl)
                )
            }
        )
        else -> component
    }
}

// Funkce pro kopírování do galerie (přesunuto z MainActivity)
suspend fun copyAllAssetsToGallery(context: Context) {
    withContext(Dispatchers.IO) {
        val assetManager = context.assets
        var copiedCount = 0
        try {
            val allFiles = assetManager.list("") ?: emptyArray()
            val imageFiles = allFiles.filter { fileName ->
                val lower = fileName.lowercase()
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
            }
            imageFiles.forEach { fileName ->
                try {
                    val mimeType = if (fileName.lowercase().endsWith(".png")) "image/png" else "image/jpeg"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "Demo_$fileName")
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.IS_PENDING, 1)
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KnihaApp")
                        }
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let { outputUri ->
                        assetManager.open(fileName).use { inputStream ->
                            resolver.openOutputStream(outputUri).use { outputStream ->
                                inputStream.copyTo(outputStream!!)
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                            resolver.update(outputUri, contentValues, null, null)
                        }
                        copiedCount++
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        } catch (e: IOException) { e.printStackTrace() }
        withContext(Dispatchers.Main) {
            if (copiedCount > 0) Toast.makeText(context, "Nahráno $copiedCount fotek", Toast.LENGTH_SHORT).show()
        }
    }
}