package com.example.cashcardkotlin.auth

import com.example.cashcardkotlin.User
import com.example.cashcardkotlin.UserRepository
import com.example.cashcardkotlin.models.LoginRequest
import com.example.cashcardkotlin.models.LoginResponse
import com.example.cashcardkotlin.models.RegisterRequest
import com.example.cashcardkotlin.models.RegisterResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/auth")
class AuthController(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val authenticationManager: AuthenticationManager
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<RegisterResponse> {

        val user = User(
            email = registerRequest.email,
            telephone = registerRequest.telephone,
            firstname = registerRequest.firstname,
            lastname = registerRequest.lastname,
            password = passwordEncoder.encode(registerRequest.password)
        )

        if (userRepository.findByEmail(registerRequest.email) != null) {
            return ResponseEntity.badRequest()
                .body(RegisterResponse(status = "Failed", message = "Account already exists"))
        }

        userRepository.save(user)

        return ResponseEntity.ok(RegisterResponse("Success", "Account created successfully!"))

    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.email,
                loginRequest.password
            )
        )

        userRepository.findByEmail(loginRequest.email)
            ?: return ResponseEntity.badRequest().body(
                LoginResponse(
                    status = "Failed",
                    message = "Wrong username or password",
                    token = "",
                    refreshToken = ""

                )
            )

        return ResponseEntity.ok(LoginResponse("Success", "Logged in successfully", "token", "refresh token"))
    }

}