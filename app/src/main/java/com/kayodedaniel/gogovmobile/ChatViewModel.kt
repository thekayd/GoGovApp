package com.kayodedaniel.gogovmobile

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayodedaniel.gogovmobile.data.Chat
import com.kayodedaniel.gogovmobile.data.ChatData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState = _chatState.asStateFlow()

    // send prompt method to system chat
    fun onEvent(context: Context, event: ChatUIEvent) {
        when (event) {
            is ChatUIEvent.sendPrompt -> {
                if (event.prompt.isNotEmpty()) {
                    addPrompt(event.prompt, event.bitmap)
                    _chatState.update { it.copy(isTyping = true) }
                    if (event.bitmap != null) {
                        // getResponseWithImage(context, event.prompt, event.bitmap)
                    } else {
                        getResponse(context, event.prompt) // Pass context here
                    }
                }
            }
            is ChatUIEvent.updatePrompt -> {
                _chatState.update {
                    it.copy(prompt = event.newPrompt)
                }
            }
        }
    }


    // adds prompt to system chat
    private fun addPrompt(prompt: String, bitmap: Bitmap?) {
        _chatState.update {
            it.copy(
                chatList = it.chatList.toMutableList().apply {
                    add(0, Chat(prompt, bitmap, true))
                },
                prompt = "",
                bitmap = null
            )
        }
    }

    // gets response for data chat
    private fun getResponse(context: Context, prompt: String) {
        viewModelScope.launch {
            try {
                val chat = ChatData.getResponse(context, prompt) // Pass context here
                _chatState.update {
                    it.copy(
                        chatList = it.chatList.toMutableList().apply {
                            add(0, chat)
                        },
                        isTyping = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching response: ${e.message}")
                _chatState.update { it.copy(isTyping = false) }
            }
        }
    }



}