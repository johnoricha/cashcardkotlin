package com.example.cashcardkotlin.cashcard.models;

import com.example.cashcardkotlin.user.User
import jakarta.persistence.*

@Entity
data class CashCard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var amount: Double? = null,
    var owner: String? = null,
    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
) {
    override fun toString(): String {
        return "CashCard(id=$id, amount='$amount', owner='$owner')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CashCard) return false

        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    fun toCashCardDto(): CashCardDto {
        return CashCardDto(id!!, amount!!, owner!!, user!!.id)
    }
}
