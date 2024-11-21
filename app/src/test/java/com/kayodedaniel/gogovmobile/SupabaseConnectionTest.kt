package com.kayodedaniel.gogovmobile

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseConnectionTest {

    private val client = OkHttpClient()

    @Test
    fun testSupabaseConnection() = runBlocking {
        val request = Request.Builder()
            .url("https://bgckkkxjfnkwgjzlancs.supabase.co")
            .build()

        client.newCall(request).execute().use { response ->
            assertTrue("Connection to Supabase failed", response.isSuccessful)
            assertEquals(200, response.code)
        }
    }
}