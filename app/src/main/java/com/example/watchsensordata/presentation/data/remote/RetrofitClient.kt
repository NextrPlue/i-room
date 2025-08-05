package com.example.watchsensordata.presentation.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.30.1.97:8080") // 또는 실제 서버 IP
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}