package com.example.cashcardkotlin.cashcard.models

import com.example.cashcardkotlin.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

data class CashCardDto(
    var id: Long? = null,
    val amount: Double? = null,
    val owner: String? = null,
    val userId: Long? = null
)
