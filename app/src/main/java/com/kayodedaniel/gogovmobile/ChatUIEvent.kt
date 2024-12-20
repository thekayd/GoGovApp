package com.kayodedaniel.gogovmobile

import android.app.VoiceInteractor.Prompt
import android.graphics.Bitmap

// sealed class for updating and sending prompts
sealed class ChatUIEvent {
    data class updatePrompt(val newPrompt: String) : ChatUIEvent()
    data class sendPrompt(
        val prompt: String,
        val bitmap: Bitmap?
    ) : ChatUIEvent()
}