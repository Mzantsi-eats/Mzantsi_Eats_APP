package com.mzantsi.table.data.api

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:5080/"

    private val authInterceptor = Interceptor { chain ->

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            chain.proceed(chain.request())
        }
        else {
                val token = try {
                    Tasks.await(firebaseUser.getIdToken(false), 10, TimeUnit.SECONDS).token
                }

                catch (e: Exception) {
                    null
                }

                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }.build()

            chain.proceed(request)
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: MzantsiApiService = Retrofit.Builder().baseUrl(BASE_URL)
        .client(okHttpClient).addConverterFactory(
        GsonConverterFactory.create())
        .build().create(MzantsiApiService::class.java)
}