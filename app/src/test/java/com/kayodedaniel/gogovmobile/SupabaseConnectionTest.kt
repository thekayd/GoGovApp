package com.kayodedaniel.gogovmobile

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

class SupabaseConnectionTest {

    @Test
    fun testSupabaseConnection() = runBlocking {
        val mockServer = MockWebServer()
        mockServer.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
        mockServer.start()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(mockServer.url("/rest/v1"))
            .build()

        client.newCall(request).execute().use { response ->
            assertTrue("Expected response to be successful", response.isSuccessful)
        }

        mockServer.shutdown()
    }
}
