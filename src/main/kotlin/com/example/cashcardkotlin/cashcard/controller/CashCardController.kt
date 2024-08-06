package com.example.cashcardkotlin.cashcard.controller

import com.example.cashcardkotlin.cashcard.repository.CashCardRepository
import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.cashcard.models.CashCard
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@RestController
@RequestMapping("/cashcards")
class CashCardController(val cashCardRepository: CashCardRepository, val userDetailsService: UserDetailsService) {

    @GetMapping("/{requestedId}")
    fun findById(@PathVariable requestedId: Long, principal: Principal): ResponseEntity<CashCard> {

        val card = cashCardRepository.findByIdAndOwner(requestedId, principal.name)

        if (card != null) {
            return ResponseEntity.ok(card)
        }
        return ResponseEntity.notFound().build()
    }

    @GetMapping
    private fun findAll(pageable: Pageable, principal: Principal): ResponseEntity<List<CashCard>> {

        val userDetails = userDetailsService.loadUserByUsername(principal.name) as User


        if (userDetails.role == Role.ADMIN) {
            val pageOfCashCards = cashCardRepository.findAll(
                PageRequest.of(
                    pageable.pageNumber,
                    pageable.pageSize,
                    pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount", "id"))
                )
            )
            return ResponseEntity.ok(pageOfCashCards.content)
        }

        println("principal: $principal")
        val pageOfCashCards = cashCardRepository.findByOwner(
            principal.name, PageRequest.of(
                pageable.pageNumber,
                pageable.pageSize,
                pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
            )
        )
        return ResponseEntity.ok(pageOfCashCards.content)
    }

    @PostMapping
    private fun createCashCard(
        @RequestBody cashCardRequest: CashCardRequest, uriComponentsBuilder: UriComponentsBuilder,
        principal: Principal
    ): ResponseEntity<String> {

        if (cashCardRequest.amount == 0.0) {
            return ResponseEntity.badRequest().body("amount cannot be null")
        }

            val newCashCard = cashCardRequest.toCashCard(amount = cashCardRequest.amount, owner = principal.name)

        val savedCashCard = cashCardRepository.save(newCashCard)

        val locationOfSavedCard = uriComponentsBuilder
            .path("/cashcards/{id}")
            .buildAndExpand(savedCashCard.id)
            .toUri()

        return ResponseEntity.created(locationOfSavedCard).build()

    }

    @PutMapping("/{id}")
    fun putCashCard(
        @PathVariable id: Long,
        @RequestBody updatedCashCardRequest: CashCard,
        principal: Principal
    ): ResponseEntity<Void> {

        val cashCard = cashCardRepository.findByIdAndOwner(id, principal.name)

        if (cashCard != null) {

            if (cashCardRepository.existsByIdAndOwner(id, principal.name)) {
                cashCardRepository.save(
                    cashCard.copy(
                        id = id,
                        amount = updatedCashCardRequest.amount,
                        owner = principal.name
                    )
                )
                return ResponseEntity.noContent().build()
            }
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/{id}")
    fun deleteCashCard(@PathVariable id: Long, principal: Principal): ResponseEntity<Void> {
        if (cashCardRepository.existsByIdAndOwner(id, principal.name)) {
            cashCardRepository.deleteById(id)
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.notFound().build()
    }
}