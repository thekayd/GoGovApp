package com.kayodedaniel.gogovmobile.data

import android.content.Context
import android.util.Log
import com.kayodedaniel.gogovmobile.R
import com.kayodedaniel.gogovmobile.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader

// Object class for the chat data and system prompts as well as response access
object ChatData {

    @SuppressWarnings("all")
    private var API_KEY: String? = null
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"

    fun initializeApiKey(context: Context) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.api_key)
            val reader = BufferedReader(InputStreamReader(inputStream))
            API_KEY = reader.readLine()
            inputStream.close()
        } catch (e: Exception) {
            Log.e("ChatData", "Failed to read API key file: ${e.message}")
        }
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geminiApiService = retrofit.create(GeminiApiService::class.java)

    private val SYSTEM_PROMPT = """
    You are an AI assistant for the GoGov Mobile App. Provide accurate, helpful information about:
    - App features and navigation
    - Government services (Home Affairs, Transportation, Health, Education)
    - User registration and account management
    - Application processes
    - Appointment scheduling

    Email Support:
    - For personalized assistance beyond chatbot capabilities
    - Official support email: support@gogov.gov.za
    - Response time: 1-2 business days
    - Include app version, device details in email

    Escalation Path:
    If unable to resolve issue:
    1. Check 'Feedback' in app Settings
    2. Use 'Report Issue' in Settings page
    3. Contact support via official email

    Key Knowledge Sources:
    ${ChatbotKnowledge.APP_OVERVIEW}
    ${ChatbotKnowledge.FEATURES}
    ${ChatbotKnowledge.USER_TYPES}
    ${ChatbotKnowledge.NAVIGATION_GUIDE}

    Always:
    - Be precise and informative
    - Guide users through app functionality
    - Respect user privacy
    - Provide clear, step-by-step instructions
    """

    suspend fun getResponse(context: Context, prompt: String): Chat {
        if (API_KEY == null) initializeApiKey(context)

        return try {
            val request = GeminiRequest(
                contents = listOf(
                    Content(parts = listOf(
                        Part(text = SYSTEM_PROMPT),
                        Part(text = prompt)
                    ))
                )
            )

            val response = withContext(Dispatchers.IO) {
                geminiApiService.generateContent(API_KEY.orEmpty(), request) // Safely using API_KEY
            }

            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I couldn't generate a response."

            val finalResponse = if (responseText.contains("I apologize") || responseText.length < 10) {
                "I'm unable to fully resolve your query. Please:\n" +
                        "1. Visit 'Feedback' in app Settings\n" +
                        "2. Use 'Report Issue' in Settings\n" +
                        "3. Email support@gogov.gov.za for personalized assistance"
            } else {
                responseText
            }

            Chat(
                prompt = finalResponse,
                bitmap = null,
                isFromUser = false
            )
        } catch (e: Exception) {
            Log.e("ChatData", "Error generating response: ${e.message}")
            Chat(
                prompt = "Sorry, there was an error. Please:\n" +
                        "1. Check your internet connection\n" +
                        "2. Visit 'Feedback' in Settings\n" +
                        "3. Email support@gogov.gov.za for help",
                bitmap = null,
                isFromUser = false
            )
        }
    }
}
