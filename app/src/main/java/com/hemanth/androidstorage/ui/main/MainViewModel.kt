package com.hemanth.androidstorage.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.hemanth.androidstorage.data.model.InternalStoragePhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException

class MainViewModel : ViewModel() {

    internal fun savePhotoToInternalStorage(
        fileName: String,
        bmp: Bitmap,
        context: Context
    ): Boolean = try {
        context.openFileOutput("$fileName.jpg", AppCompatActivity.MODE_PRIVATE)
            .use { fileOutputStream: FileOutputStream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)) {
                    throw IOException("Couldn't save Bitmap")
                }
            }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    internal suspend fun loadPhotosFromInternalStorage(
        context: Context
    ): List<InternalStoragePhoto> =
        withContext(Dispatchers.IO) {
            val files = context.filesDir.listFiles()
            files?.filter { it.isFile && it.canRead() && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }

    internal fun deletePhotoFromInternalStorage(
        filename: String,
        context: Context
    ): Boolean = try {
        context.deleteFile(filename)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

}
