package com.example.watchsensordata.presentation.data.model

data class LoginResponse(
    val status: String,
    val message: String,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val timestamp: String
)