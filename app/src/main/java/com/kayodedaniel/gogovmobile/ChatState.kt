package com.kayodedaniel.gogovmobile

import android.graphics.Bitmap
import com.kayodedaniel.gogovmobile.data.Chat

data class ChatState(
    val chatList: MutableList<Chat> = mutableListOf(),
    val prompt: String = "",
    val bitmap: Bitmap? = null,
    val isTyping: Boolean = false
)
