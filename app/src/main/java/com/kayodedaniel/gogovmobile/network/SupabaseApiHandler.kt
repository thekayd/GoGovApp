package com.kayodedaniel.gogovmobile.network

import okhttp3.Request

class SupabaseApiHandler : BaseApiHandler() {
    override fun createRequest(data: String, url: String, apiKey: String): Request {
        return Request.Builder()
            .url(url)
            .post(createJsonBody(data))
            .addHeader("apikey", apiKey)
            .addHeader("Content-Type", "application/json")
            .build()
    }
}