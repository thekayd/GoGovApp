// BaseApiHandler.kt
package com.kayodedaniel.gogovmobile.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

abstract class BaseApiHandler {
    protected val client = OkHttpClient()

    abstract fun createRequest(data: String, url: String, apiKey: String): Request

    protected fun createJsonBody(data: String) =
        data.toRequestBody("application/json".toMediaTypeOrNull())
}


