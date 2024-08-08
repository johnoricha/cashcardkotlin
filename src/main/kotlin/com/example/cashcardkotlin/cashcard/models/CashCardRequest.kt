package com.example.cashcardkotlin.cashcard.models

import com.example.cashcardkotlin.user.User

data class CashCardRequest(val amount: Double) {

    fun toCashCard(amount: Double, owner: String, user: User): CashCard {
        return CashCard(null, amount, owner, user)
    }
}