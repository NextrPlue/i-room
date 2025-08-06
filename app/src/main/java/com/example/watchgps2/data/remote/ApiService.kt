package com.example.watchgps2.data.remote

import com.example.watchgps2.data.model.ApiKeyRequest
import com.example.watchgps2.data.model.TokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @POST("/api/user/systems/authenticate")
    fun authenticate(@Body body: ApiKeyRequest): Call<TokenResponse>
}