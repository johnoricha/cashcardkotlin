package com.example.cashcardkotlin.cashcard.controller

import com.example.cashcardkotlin.cashcard.models.CashCard
import com.example.cashcardkotlin.cashcard.models.CashCardDto
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import com.example.cashcardkotlin.cashcard.service.CashCardService
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@RestController
@RequestMapping("/cashcards")
class CashCardController(
    val cashCardService: CashCardService,
) {

    @GetMapping("/{requestedId}")
    fun findById(@PathVariable requestedId: Long, principal: Principal): ResponseEntity<CashCardDto> {
        return cashCardService.findCashCardById(requestedId, principal)
    }

    @GetMapping
    private fun findAll(pageable: Pageable, principal: Principal): ResponseEntity<List<CashCardDto>> {
        return cashCardService.findAllCashCards(pageable, principal)
    }

    @PostMapping
    private fun createCashCard(
        @RequestBody cashCardRequest: CashCardRequest, uriComponentsBuilder: UriComponentsBuilder,
        principal: Principal
    ): ResponseEntity<String> {
        return cashCardService.createCashCard(cashCardRequest, uriComponentsBuilder, principal)
    }

    @PutMapping("/{id}")
    fun putCashCard(
        @PathVariable id: Long,
        @RequestBody updatedCashCardRequest: CashCard,
        principal: Principal
    ): ResponseEntity<Void> {
        return cashCardService.updateCashCard(id, updatedCashCardRequest, principal)
    }

    @DeleteMapping("/{id}")
    fun deleteCashCard(@PathVariable id: Long, principal: Principal): ResponseEntity<Void> {
        return cashCardService.deleteCashCard(id, principal)
    }
}