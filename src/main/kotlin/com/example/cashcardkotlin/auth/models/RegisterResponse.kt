package com.example.cashcardkotlin.auth.models

data class RegisterResponse(
    val status: String,
    val message: String,
    val accessToken: String,
)
