import com.example.cashcardkotlin.cashcard.models.CashCard
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import com.example.cashcardkotlin.cashcard.repository.CashCardRepository
import com.example.cashcardkotlin.cashcard.service.CashCardService
import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@ExtendWith(MockitoExtension::class)
class CashCardServiceTest {

    @Mock
    private lateinit var cashCardRepository: CashCardRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userDetailsService: UserDetailsService

    @InjectMocks
    private lateinit var cashCardService: CashCardService

    private val principal: Principal = Mockito.mock(Principal::class.java)

    @Test
    fun `findCashCardById should return CashCardDto when card is found`() {
        // Arrange
        val requestedId = 1L
        val owner = "user@xyz.com"
        Mockito.`when`(principal.name).thenReturn(owner)

        val cashCard = CashCard(requestedId, 100.0, owner, User(1, "user@example.com", Role.USER.name))
        Mockito.`when`(cashCardRepository.findByIdAndOwner(requestedId, owner)).thenReturn(cashCard)

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
        Mockito.`when`(principal.name).thenReturn(owner)
        Mockito.`when`(cashCardRepository.findByIdAndOwner(requestedId, owner)).thenReturn(null)

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
        Mockito.`when`(principal.name).thenReturn(owner1)
        Mockito.`when`(userDetailsService.loadUserByUsername(owner1)).thenReturn(userDetails)

        val pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "amount")
        val cashCards = listOf(
            CashCard(1L, 100.0, owner1, userDetails),
            CashCard(2L, 200.0, owner2, userDetails)
        )

        Mockito.`when`(cashCardRepository.findByOwner(owner1, pageable)).thenReturn(PageImpl(cashCards, pageable, 10))

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
        Mockito.`when`(principal.name).thenReturn(owner)

        val cashCardRequest = CashCardRequest(100.0)
        val user = User(1, owner, Role.USER.name)
        Mockito.`when`(userRepository.findByEmail(owner)).thenReturn(user)

        val cashCard = CashCard(1L, 100.0, owner, user)
        Mockito.`when`(cashCardRepository.save(Mockito.any(CashCard::class.java))).thenReturn(cashCard)

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
        Mockito.`when`(principal.name).thenReturn(owner)

        val existingCard = CashCard(id, 100.0, owner, User(1, owner, Role.USER.name))
        val updatedRequest = CashCard(id, 200.0, owner, existingCard.user)
        Mockito.`when`(cashCardRepository.findByIdAndOwner(id, owner)).thenReturn(existingCard)
        Mockito.`when`(cashCardRepository.existsByIdAndOwner(id, owner)).thenReturn(true)
        Mockito.`when`(userRepository.findByEmail(owner)).thenReturn(User(1, "user@xyz.com", Role.USER.name))

        // Act
        val response = cashCardService.updateCashCard(id, updatedRequest, principal)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        Mockito.verify(cashCardRepository).save(existingCard)
    }

    @Test
    fun `deleteCashCard should delete the cash card`() {
        // Arrange
        val id = 1L
        val owner = "user@xyz.com"
        Mockito.`when`(principal.name).thenReturn(owner)
        Mockito.`when`(cashCardRepository.existsByIdAndOwner(id, owner)).thenReturn(true)

        // Act
        val response = cashCardService.deleteCashCard(id, principal)

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        Mockito.verify(cashCardRepository).deleteById(id)
    }

    @Test
    fun `deleteCashCard should return 404 if card does not exist`() {
        // Arrange
        val id = 1L
        val owner = "user@xyz.com"
        Mockito.`when`(principal.name).thenReturn(owner)
        Mockito.`when`(cashCardRepository.existsByIdAndOwner(id, owner)).thenReturn(false)

        // Act
        val response = cashCardService.deleteCashCard(id, principal)

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}