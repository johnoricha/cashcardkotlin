package com.example.cashcardkotlin.cashcard.models

data class CashCardRequest(val amount: Double) {

    fun toCashCard(amount: Double, owner: String): CashCard {
        return CashCard(null, amount, owner)
    }
}