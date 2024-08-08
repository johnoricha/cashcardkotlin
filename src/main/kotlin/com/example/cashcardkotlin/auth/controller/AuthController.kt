package com.example.cashcardkotlin.auth.controller

import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.config.JwtService
import com.example.cashcardkotlin.auth.models.LoginRequest
import com.example.cashcardkotlin.auth.models.LoginResponse
import com.example.cashcardkotlin.auth.models.RegisterRequest
import com.example.cashcardkotlin.auth.models.RegisterResponse
import com.example.cashcardkotlin.auth.service.AuthService
import com.example.cashcardkotlin.user.UserRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/auth")
class AuthController(
    val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {

        return authService.register(registerRequest)

    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return authService.login(loginRequest)
    }
}