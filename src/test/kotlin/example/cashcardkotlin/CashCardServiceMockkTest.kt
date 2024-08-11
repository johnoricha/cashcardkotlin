package example.cashcardkotlin

import com.example.cashcardkotlin.cashcard.models.CashCard
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import com.example.cashcardkotlin.cashcard.repository.CashCardRepository
import com.example.cashcardkotlin.cashcard.service.CashCardService
import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.user.UserRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal


@ExtendWith(MockKExtension::class)
class CashCardServiceMockkTest {
    @MockK
    private lateinit var cashCardRepository: CashCardRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var userDetailsService: UserDetailsService

    @InjectMockKs
    private lateinit var cashCardService: CashCardService

    private val principal: Principal = mockk<Principal>()

    @Test
    fun `findCashCardById should return CashCardDto when card is found`() {
        // Arrange
        val requestedId = 1L
        val owner = "user@xyz.com"
        every { principal.name } returns owner

        val cashCard = CashCard(requestedId, 100.0, owner, User(1, "user@example.com", Role.USER.name))

        every { cashCardRepository.findByIdAndOwner(requestedId, owner) } returns cashCard

        // Act
        val response = cashCardService.findCashCardById(requestedId, principal)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(cashCard.toCashCardDto(), response.body)
    }

    @Test
    fun `findCashCardById should return 404 when card is not found`() {
        // Arrange
        val requestedId = 1L
        val owner = "user@xyz.com"
        every { principal.name } returns owner
        every { cashCardRepository.findByIdAndOwner(requestedId, owner) } returns null

        // Act
        val response = cashCardService.findCashCardById(requestedId, principal)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `findAllCashCards should return all cash cards for an admin`() {
        // Arrange
        val owner1 = "admin@xyz.com"
        val owner2 = "user@xyz.com"
        val userDetails = User(1, owner1, Role.ADMIN.name)

        every { principal.name } returns owner1
        every { userDetailsService.loadUserByUsername(owner1) } returns userDetails

        val pageable = PageRequest.of(0, 1, Sort.Direction.ASC, "amount")
        val cashCards = listOf(
            CashCard(1L, 100.0, owner1, userDetails),
            CashCard(2L, 200.0, owner2, userDetails)
        )

        every { cashCardRepository.findByOwner(owner1, pageable) } returns PageImpl(cashCards, pageable, 1)

        // Act
        val response = cashCardService.findAllCashCards(pageable, principal)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(2, response.body?.size)
    }

    @Test
    fun `createCashCard should create a new cash card`() {
        // Arrange
        val owner = "user@xyz.com"

        every { principal.name } returns owner

        val cashCardRequest = CashCardRequest(100.0)
        val user = User(1, owner, Role.USER.name)
        every { userRepository.findByEmail(owner) } returns user

        val cashCard = CashCard(1L, 100.0, owner, user)

        every { cashCardRepository.save(any()) } returns cashCard

        val uriComponentsBuilder = UriComponentsBuilder.newInstance()

        // Act
        val response = cashCardService.createCashCard(cashCardRequest, uriComponentsBuilder, principal)

        // Assert
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location.toString().contains("/cashcards/1"))
    }

    @Test
    fun `updateCashCard should update the cash card`() {
        // Arrange
        val id = 1L
        val owner = "user@xyz.com"
        every {  (principal.name) } returns owner

        val existingCard = CashCard(id, 100.0, owner, User(1, owner, Role.USER.name))
        val updatedCashCard = CashCard(id, 200.0, owner, existingCard.user)

        every { cashCardRepository.findByIdAndOwner(id, owner) } returns existingCard
        every { cashCardRepository.existsByIdAndOwner(id, owner) } returns true
        every { userRepository.findByEmail(owner) } returns User(1, "user@xyz.com", Role.USER.name)
        every { cashCardRepository.save(any()) } returns updatedCashCard

        // Act
        val response = cashCardService.updateCashCard(id, updatedCashCard, principal)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify { cashCardRepository.save(existingCard) }
    }

    @Test
    fun `deleteCashCard should delete the cash card`() {
        // Arrange
        val id = 1L
        val owner = "user@xyz.com"

        every {  (principal.name) } returns owner
        every { cashCardRepository.existsByIdAndOwner(id, owner) } returns true
        every { cashCardRepository.deleteById(id) } returns Unit

        // Act
        val response = cashCardService.deleteCashCard(id, principal)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        verify { cashCardRepository.deleteById(id) }
    }

    @Test
    fun `deleteCashCard should return 404 if card does not exist`() {
        // Arrange
        val id = 1L
        val owner = "user@xyz.com"

        every {  (principal.name) } returns owner
        every { cashCardRepository.existsByIdAndOwner(id, owner) } returns false

        // Act
        val response = cashCardService.deleteCashCard(id, principal)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

}