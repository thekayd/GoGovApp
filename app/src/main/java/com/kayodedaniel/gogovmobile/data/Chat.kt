package com.kayodedaniel.gogovmobile.data

import android.graphics.Bitmap

//data class
data class Chat(
    val prompt: String,
    val bitmap: Bitmap?,
    val isFromUser: Boolean
)

