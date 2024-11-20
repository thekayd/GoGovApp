package com.kayodedaniel.gogovmobile.chatbotactivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kayodedaniel.gogovmobile.ChatUIEvent
import com.kayodedaniel.gogovmobile.ChatViewModel
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.ui.theme.GeminiChatBotTheme

class ChatBotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiChatBotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary)
                                    .height(55.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    text = "GoGov ChatBot",
                                    fontSize = 19.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    ) { paddingValues ->
                        ChatScreen(paddingValues = paddingValues)
                    }
                }
            }
        }
    }

    @Composable
    fun ChatScreen(paddingValues: PaddingValues) {
        val chatViewModel = viewModel<ChatViewModel>()
        val chatState = chatViewModel.chatState.collectAsState().value

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.Bottom
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                reverseLayout = true
            ) {
                if (chatState.isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
                itemsIndexed(chatState.chatList) { _, chat ->
                    if (chat.isFromUser) {
                        UserChatItem(prompt = chat.prompt)
                    } else {
                        ModelChatItem(response = chat.prompt)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = chatState.prompt,
                    onValueChange = {
                        chatViewModel.onEvent(ChatUIEvent.updatePrompt(it))
                    },
                    placeholder = {
                        Text(text = "Ask about GoGov services...")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                IconButton(
                    onClick = {
                        if (chatState.prompt.isNotEmpty()) {
                            chatViewModel.onEvent(ChatUIEvent.sendPrompt(chatState.prompt, null))
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send Prompt",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    fun UserChatItem(prompt: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = prompt,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

    @Composable
    fun ModelChatItem(response: String) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = response,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }

    @Composable
    fun TypingIndicator() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Typing...",
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}