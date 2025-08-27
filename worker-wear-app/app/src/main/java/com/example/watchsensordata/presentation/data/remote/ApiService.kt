package com.example.watchsensordata.presentation.data.remote

import com.example.watchsensordata.presentation.data.model.LoginRequest
import com.example.watchsensordata.presentation.data.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("/api/user/workers/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}