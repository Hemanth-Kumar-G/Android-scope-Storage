package com.hemanth.androidstorage.ui.main

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.hemanth.androidstorage.data.model.InternalStoragePhoto
import com.hemanth.androidstorage.util.sdk29AndUp
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

    internal fun savePhotoExternalStorage(
        context: Context,
        displayName: String,
        bmp: Bitmap
    ): Boolean {
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        return try {
            context.contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                context.contentResolver.openOutputStream(uri).use { outputStream ->
                    if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }
}
