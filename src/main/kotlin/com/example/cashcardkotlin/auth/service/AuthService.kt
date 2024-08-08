package com.example.cashcardkotlin.auth.service

import com.example.cashcardkotlin.auth.models.LoginRequest
import com.example.cashcardkotlin.auth.models.LoginResponse
import com.example.cashcardkotlin.auth.models.RegisterRequest
import com.example.cashcardkotlin.auth.models.RegisterResponse
import com.example.cashcardkotlin.config.JwtService
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.user.UserRepository
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val authenticationManager: AuthenticationManager,
    val jwtService: JwtService,
) {

    fun register(registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {
        val user = User(
            email = registerRequest.email,
            telephone = registerRequest.telephone,
            firstname = registerRequest.firstname,
            lastname = registerRequest.lastname,
            password = passwordEncoder.encode(registerRequest.password),
            role = registerRequest.role
        )

        if (userRepository.findByEmail(registerRequest.email) != null) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(status = "Failed", message = "Account already exists", accessToken = ""))
        }

        val jwt = jwtService.generateToken(user)

        userRepository.save(user)

        return ResponseEntity.ok(RegisterResponse("Success", "Account created successfully!", jwt))

    }

    fun login(loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password
            )
        )

        val user = userRepository.findByEmail(loginRequest.email)
            ?: return ResponseEntity.badRequest().body(
                LoginResponse(
                    status = "Failed",
                    message = "Wrong username or password",
                    token = "",
                    refreshToken = ""

                )
            )

        val jwt = jwtService.generateToken(user)

        return ResponseEntity.ok(LoginResponse("Success", "Logged in successfully", jwt, "refresh token"))
    }
}