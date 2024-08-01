package com.example.cashcardkotlin

data class CashCardRequest(val amount: Double) {

    fun toCashCard(amount: Double, owner: String): CashCard {
        return CashCard(null, amount, owner)
    }
}