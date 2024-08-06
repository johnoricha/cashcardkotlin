package com.example.cashcardkotlin

import com.example.cashcardkotlin.auth.models.RegisterResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<RegisterResponse> {
        val errors: MutableMap<String, String?> = HashMap()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage
            errors[fieldName] = errorMessage
        }

        if (errors.containsKey("firstname")) {
            return ResponseEntity.badRequest().body(
                RegisterResponse(
                    status = "Failed",
                    message = errors["firstname"]!!,
                    accessToken = ""
                )
            )
        }

        if (errors.containsKey("lastname")) {
            return ResponseEntity.badRequest().body(
                RegisterResponse(
                    status = "Failed",
                    message = errors["lastname"]!!,
                    accessToken = ""
                )
            )
        }

        if (errors.containsKey("role")) {
            return ResponseEntity.badRequest().body(
                RegisterResponse(
                    status = "Failed",
                    message = errors["role"]!!,
                    accessToken = ""
                )
            )
        }

        if (errors.containsKey("telephone") && errors.containsKey("email")) {
            return ResponseEntity.badRequest().body(
                RegisterResponse(
                    status = "Failed",
                    message = "Please provide an email or phone number to login",
                    accessToken = ""
                )
            )
        }

        return ResponseEntity.badRequest().body(
            RegisterResponse(
                status = "Failed",
                message = "Email and Password are required",
                accessToken = ""
            )
        )
    }
}