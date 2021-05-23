package com.hemanth.androidstorage.data.model

import android.net.Uri

data class SharedStoragePhoto( val id: Long,
                               val name: String,
                               val width: Int,
                               val height: Int,
                               val contentUri: Uri
)
