package com.hemanth.androidstorage.util

import android.os.Build


inline fun <T> sdk29AndUp(onSdk29: () -> T): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        onSdk29()
    else null

