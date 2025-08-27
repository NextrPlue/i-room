package com.example.watchgps2.data.remote

import android.content.Context
import com.example.watchgps2.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenManager.getToken(context)
        val newRequest = chain.request().newBuilder().apply {
            token?.let {
                header("Authorization", "Bearer $it")
            }
        }.build()
        return chain.proceed(newRequest)
    }
}