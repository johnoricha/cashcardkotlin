package com.example.cashcardkotlin.auth.models

import com.example.cashcardkotlin.user.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class RegisterRequest (
    @field:NotBlank(message = "Email is required") val email: String = "",
    @field:NotBlank(message = "Password is required") val password: String = "",
    @field:NotBlank(message = "Firstname is required") val firstname: String?,
    @field:NotBlank(message = "Lastname is required") val lastname: String?,
    @field:NotBlank(message = "Telephone is required") val telephone: String?,
    @field:NotNull(message = "Role is required") val role: Role?,
)
