package com.example.cashcardkotlin.models

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    val email: String = "",
    val telephone: String = "",
    @field:NotBlank(message = "Password is required") val password: String = ""
)
