package com.example.cashcardkotlin.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun findByEmail(email: String): User?

}