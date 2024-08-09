package example.cashcardkotlin

import com.example.cashcardkotlin.CashCardKotlinApplication
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CashCardKotlinApplication::class]
)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private lateinit var headers: HttpHeaders

    @BeforeEach
    fun setup() {
        headers = HttpHeaders().apply {
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }
    }

    private fun registerUser(role: Role = Role.OWNER): String {

        println("registerUser: role: ${role.ordinal}")
        val user = User(
            email = "user${role.ordinal}@xyz.com",
            password = "Test@123",
            telephone = "09128383322",
            firstname = "User",
            lastname = "Smith",
            role = role
        )

        val response = restTemplate.postForEntity("/auth/register", user, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        return JsonPath.parse(response.body).read("$.accessToken")
    }

    private fun createCashCard(token: String, amount: Double): ResponseEntity<String> {
        headers.setBearerAuth(token)
        val request = HttpEntity(CashCardRequest(amount), headers)
        return restTemplate.exchange("/cashcards", HttpMethod.POST, request, String::class.java)
    }

    @Test
    fun shouldRegisterNewUser() {
        val token = registerUser()
        assertThat(token).isNotEmpty()
    }

    @Test
    fun shouldReturnACashCardWhenDataIsSaved() {
        val token = registerUser()

        val createCardResponse = createCashCard(token, 100.0)
        assertThat(createCardResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val response = restTemplate.exchange(
            "/cashcards/1", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )
        val documentContext = JsonPath.parse(response.body)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(documentContext.read<Number>("$.id")).isNotNull()
        assertThat(documentContext.read<Double>("$.amount")).isEqualTo(100.0)
    }

    @Test
    fun shouldNotReturnACashCardWithAnUnknownId() {
        val token = registerUser()
        val response = restTemplate.exchange(
            "/cashcards/1000", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldCreateANewCashCard() {
        val token = registerUser()
        val createCardResponse = createCashCard(token, 250.0)
        assertThat(createCardResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val location = createCardResponse.headers.location
        val response = restTemplate.exchange(location.toString(), HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun shouldReturnAListCashCards() {
        val token = registerUser()
        listOf(250.0, 300.0).forEach { createCashCard(token, it) }

        val response = restTemplate.exchange(
            "/cashcards", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )

        val cashCardCount = JsonPath.parse(response.body).read<Int>("$.length()")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(cashCardCount).isGreaterThan(1)
    }

    @Test
    fun shouldReturnAPageOfCashCards() {
        val token = registerUser()
        listOf(250.0, 300.0).forEach { createCashCard(token, it) }

        val response = restTemplate.exchange(
            "/cashcards?page=0&size=1", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )

        val page = JsonPath.parse(response.body).read<JSONArray>("$[*]")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(page.size).isEqualTo(1)
    }

    @Test
    fun shouldReturnASortedPageOfCashCards() {
        val token = registerUser()
        listOf(250.0, 300.0).forEach { createCashCard(token, it) }

        val response = restTemplate.exchange(
            "/cashcards?page=0&size=1&sort=amount,desc", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )

        val firstAmount = JsonPath.parse(response.body).read<Double>("$[0].amount")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(firstAmount).isEqualTo(300.0)
    }

    @Test
    fun shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        val token = registerUser()
        listOf(250.0, 300.0).forEach { createCashCard(token, it) }

        val response = restTemplate.exchange(
            "/cashcards", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )

        val amounts = JsonPath.parse(response.body).read<JSONArray>("$..amount")
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(amounts).containsExactly(250.0, 300.0)
    }

    @Test
    fun shouldNotReturnACashCardWhenUsingBadCredentials() {
        val token = registerUser()
        listOf(250.0, 300.0).forEach { createCashCard(token, it) }

        headers.setBearerAuth("BadToken")
        val response = restTemplate.exchange(
            "/cashcards/1", HttpMethod.GET, HttpEntity<Void>(headers), String::class.java
        )

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun shouldRejectUsersWhoAreNotCardOwners() {
        val token = registerUser(Role.USER)
        val response = restTemplate.exchange(
            "/cashcards/1", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        val token = registerUser()
        val response = restTemplate.exchange(
            "/cashcards/1", HttpMethod.GET, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), String::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldUpdateAnExistingCashCard() {
        val token = registerUser()
        val createCardResponse = createCashCard(token, 250.0)
        assertThat(createCardResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        println("createdCardResponse= ${createCardResponse.body}")

        val cashCardUri = createCardResponse.headers.location
        val updateResponse = restTemplate.exchange(
            cashCardUri, HttpMethod.PUT, HttpEntity(CashCardRequest(300.0), headers.apply { setBearerAuth(token) }), String::class.java
        )

        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }

    @Test
    @DirtiesContext
    fun shouldNotDeleteACashCardThatDoesNotExist() {
        val token = registerUser()
        val deleteResponse = restTemplate.exchange(
            "/cashcards/99999", HttpMethod.DELETE, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), Void::class.java
        )
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
        val token = registerUser()
        listOf(CashCardRequest(250.0), CashCardRequest(300.0)).forEach { card ->
            val createResponse = restTemplate.exchange(
                "/cashcards", HttpMethod.POST, HttpEntity(card, headers.apply { setBearerAuth(token) }), Void::class.java
            )
            assertThat(createResponse.statusCode).isEqualTo(HttpStatus.CREATED)
        }
        val deleteResponse = restTemplate.exchange(
            "/cashcards/3", HttpMethod.DELETE, HttpEntity<Void>(headers.apply { setBearerAuth(token) }), Void::class.java
        )

        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }
}