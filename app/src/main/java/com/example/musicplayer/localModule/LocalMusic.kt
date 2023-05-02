package com.example.musicplayer.localModule

import android.graphics.Bitmap

data class LocalMusic(
    val index: Int,
    val title: String,
    val author: String,
    val data: String? = null,
    val pic: Bitmap?
)
