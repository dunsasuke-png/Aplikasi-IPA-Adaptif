package com.app.manfaattumbuhan.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Supabase direct connection
    const val SUPABASE_URL = "https://nbgjggkhubmpbxmjtpgt.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5iZ2pnZ2todWJtcGJ4bWp0cGd0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc3MTU2NjcsImV4cCI6MjA5MzI5MTY2N30.BiIkzmuqyYq-9KUVyCydj6_jUhi9QHtmKQn1Wi-blsY"

    private const val BASE_URL = "$SUPABASE_URL/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_KEY")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
