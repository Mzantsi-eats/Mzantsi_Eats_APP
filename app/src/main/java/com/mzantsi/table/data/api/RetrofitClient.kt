package com.mzantsi.table.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Creates the Retrofit client that talks to our API.
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:5080/" // 10.0.2.2 is your computer when you use the emulator. On a real phone, use your computer's IP address.

    // Short timeouts, so the app doesn't wait long when the API is not running.
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .build()

    val api: MzantsiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MzantsiApiService::class.java)
    }
}
