package com.example.cashcardkotlin.auth.models

data class LoginResponse(
    val status: String,
    val message: String,
    val token: String,
    val refreshToken: String
)
