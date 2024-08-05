package com.example.cashcardkotlin.models

import jakarta.validation.constraints.NotBlank

data class RegisterRequest (
    @field:NotBlank(message = "Email is required") val email: String = "",
    @field:NotBlank(message = "Password is required") val password: String = "",
    @field:NotBlank(message = "Firstname is required") val firstname: String?,
    @field:NotBlank(message = "Lastname is required") val lastname: String?,
    @field:NotBlank(message = "Telephone is required") val telephone: String?,
)
