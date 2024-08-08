package example.cashcardkotlin

import com.example.cashcardkotlin.CashCardKotlinApplication
import com.example.cashcardkotlin.cashcard.controller.CashCardController
import com.example.cashcardkotlin.cashcard.models.CashCard
import com.example.cashcardkotlin.cashcard.models.CashCardRequest
import com.example.cashcardkotlin.user.Role
import com.example.cashcardkotlin.user.User
import com.example.cashcardkotlin.user.UserRepository
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CashCardKotlinApplication::class]
)
@ComponentScan(basePackages = ["com.example.cashcardkotlin"])
@ActiveProfiles("test")
@EnableAutoConfiguration
@Configuration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CashCardApplicationTests {
    @Autowired
    private lateinit var cashCardController: CashCardController

    @Autowired lateinit var userRepository: UserRepository

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @DirtiesContext
    fun shouldRegisterNewUser() {

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @Throws(JsonProcessingException::class)
    @DirtiesContext
    fun shouldReturnACashCardWhenDataIsSaved() {

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user3@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User3",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        val httpEntity = HttpEntity(CashCard(null, 100.0, null), headers)

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Save new Card
        val createCardResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, String::class.java)
        assertThat(createCardResponse.statusCode).isEqualTo(HttpStatus.CREATED)


        val response: ResponseEntity<String> = restTemplate
            .exchange("/cashcards/1", HttpMethod.GET, httpEntity, String::class.java)


        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)
        val id = documentContext.read<Number>("$.id")
        assertThat(id).isNotNull()

        val amount = documentContext.read<Double>("$.amount")
        assertThat(amount).isEqualTo(100.0)
    }

    @Test
    @Throws(JsonProcessingException::class)
    @DirtiesContext
    fun shouldNotReturnACashCardWithAnUnknownId() {
        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user3@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User3",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        val httpEntity = HttpEntity(CashCard(null, 100.0, null), headers)

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val response: ResponseEntity<String> = restTemplate
            .exchange(
                "/cashcards/1000", HttpMethod.GET, httpEntity, String::class.java
            )

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @Throws(JsonProcessingException::class)
    @DirtiesContext
    fun shouldCreateANewCashCard() {
        val cashCard: CashCard = CashCard(null, 250.00, null)

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        val httpEntity = HttpEntity(cashCard, headers)

        val createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)

        println("createResponse: $createResponse")
        println("Response Body: ${createResponse.body}")
        println("Response Headers: ${createResponse.headers}")

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val locationOfCreatedCashCard = createResponse.headers.location

        val getResponse: ResponseEntity<String> = restTemplate
            .exchange(locationOfCreatedCashCard.toString(), HttpMethod.GET, httpEntity, String::class.java)

//        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)

//        val jsonString = objectMapper.writeValueAsString(getResponse.getBody())
//
//        val documentContext = JsonPath.parse(jsonString)
//        val id = documentContext.read<Number>("$.id")
//        val amount = documentContext.read<Double>("$.amount")
//
//        assertThat(id).isNotNull()
//        assertThat(amount).isEqualTo(250.00)
    }

    @Test
    @DirtiesContext
    fun shouldReturnAListCashCards() {

        val cards = listOf(
            CashCard(null, 250.00, null),
            CashCard(null, 300.00, null),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCard>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)


        }

        println("createResponse: $createResponse")
        println("Response Body: ${createResponse?.body}")
        println("Response Headers: ${createResponse?.headers}")

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        // fetch cards

        val response = restTemplate
            .exchange("/cashcards", HttpMethod.GET, httpEntity, String::class.java)

        println("shouldReturnAList response: $response")

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)

        val cashCardCount = documentContext.read<Int>("$.length()")
        assertThat(cashCardCount).isGreaterThan(1)
    }

    @Test
    @DirtiesContext
    fun shouldReturnAPageOfCashCards() {
        val cards = listOf(
            CashCard(null, 250.00, null),
            CashCard(null, 300.00, null),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCard>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)


        }

        println("createResponse: $createResponse")
        println("Response Body: ${createResponse?.body}")
        println("Response Headers: ${createResponse?.headers}")

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        val response = restTemplate
            .exchange("/cashcards?page=0&size=1", HttpMethod.GET, httpEntity, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)
        val page = documentContext.read<JSONArray>("$[*]")
        assertThat(page.size).isEqualTo(1)
    }

    @Test
    @DirtiesContext
    fun shouldReturnASortedPageOfCashCards() {

        val cards = listOf(
            CashCard(null, 250.00, null),
            CashCard(null, 300.00, null),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCard>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)

        }

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        val response = restTemplate
            .exchange("/cashcards?page=0&size=1&sort=amount,desc", HttpMethod.GET, httpEntity, String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)

        val readPage = documentContext.read<JSONArray>("$[*]")
        assertThat(readPage.size).isEqualTo(1)

        val firstAmount = documentContext.read<Double>("$[0].amount")
        assertThat(firstAmount).isEqualTo(300.0)
    }

    @Test
    @DirtiesContext
    fun shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {

        val cards = listOf(
            CashCard(null, 250.00, null),
            CashCard(null, 300.00, null),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCard>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)

        }

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        val response = restTemplate
            .exchange("/cashcards", HttpMethod.GET, httpEntity, String::class.java) // default sort order -> asc
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)
        val page = documentContext.read<JSONArray>("$[*]")
        assertThat(page.size).isEqualTo(2)

        val amounts = documentContext.read<JSONArray>("$..amount")
        assertThat(amounts).containsExactly(250.0, 300.0)
    }

    @Test
    @DirtiesContext
    fun shouldNotReturnACashCardWhenUsingBadCredentials() {

        val cards = listOf(
            CashCard(null, 250.00, null),
            CashCard(null, 300.00, null),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCard>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)

        }

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        val badHheaders = LinkedMultiValueMap<String, String>()

        badHheaders.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer Badtoken"
        )
        badHheaders.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )
        val response = restTemplate
            .exchange("/cashcards/1", HttpMethod.GET, HttpEntity<MultiValueMap<String, String>>(badHheaders), String::class.java)
        println("status code: " + response.statusCode)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)

    }

    @Test
    @DirtiesContext
    fun shouldRejectUsersWhoAreNotCardOwners() {

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.USER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")

        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        val response = restTemplate
            .exchange("/cashcards/1", HttpMethod.GET, HttpEntity<MultiValueMap<String, String>>(headers), String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    @DirtiesContext
    fun shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")


        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        val httpEntity = HttpEntity<MultiValueMap<String, String>>(headers)

        val response = restTemplate
            .exchange("/cashcards/1", HttpMethod.GET, httpEntity, String::class.java) // kumar2's data
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldUpdateAnExistingCashCard() {
        val cards = listOf(
            CashCardRequest(250.00),
            CashCardRequest(300.00),
        )

        // Register User
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")

        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $token")
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json")

        var createResponse: ResponseEntity<Void>? = null
        var cashCardId: Long? = null

        cards.forEach { card ->
            val httpRequestEntity = HttpEntity(card, headers)
            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpRequestEntity, Void::class.java)

            // Fetch the ID of the created CashCard from the "Location" header
            val locationUri = createResponse?.headers?.location
            cashCardId = locationUri?.path?.split("/")?.last()?.toLong()
        }

        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(cashCardId).isNotNull()

        // Update the CashCard with a new amount
        val updateRequestEntity = HttpEntity(CashCardRequest(50.0), headers)
        val updateResponse = restTemplate.exchange("/cashcards/$cashCardId", HttpMethod.PUT, updateRequestEntity, Void::class.java)
        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // Fetch the updated CashCard and verify the changes
        val getResponse = restTemplate.exchange("/cashcards/$cashCardId", HttpMethod.GET, HttpEntity(null, headers), String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(getResponse.body)
        val id = documentContext.read<Number>("$.id")
        val amount = documentContext.read<Double>("$.amount")
        assertThat(id).isEqualTo(cashCardId?.toInt())
        assertThat(amount).isEqualTo(50.0)
    }

    @Test
    fun shouldNotUpdateACashCardThatDoesNotExist() {
        val unknownCard = CashCardRequest( 19.99)

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        // Verify registration is successful
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Step 2: Extract the JWT token from the registration response
        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")
        assertThat(token).isNotBlank

        // Step 3: Set up authorization headers with the JWT token
        val headers = LinkedMultiValueMap<String, String>().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        val request = HttpEntity<CashCardRequest>(unknownCard, headers)
        val response = restTemplate
            .exchange("/cashcards/99999", HttpMethod.PUT, request, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        val someoneElseCard = CashCardRequest( 333.33)

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        // Verify registration is successful
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Step 2: Extract the JWT token from the registration response
        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")
        assertThat(token).isNotBlank

        // Step 3: Set up authorization headers with the JWT token
        val headers = LinkedMultiValueMap<String, String>().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        val request = HttpEntity<CashCardRequest>(someoneElseCard, headers)
        val response = restTemplate
            .exchange("/cashcards/102", HttpMethod.PUT, request, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldDeleteAnExistingCashCard() {
        // Step 1: Register a new user
        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        // Verify registration is successful
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Step 2: Extract the JWT token from the registration response
        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")
        assertThat(token).isNotBlank

        // Step 3: Set up authorization headers with the JWT token
        val headers = LinkedMultiValueMap<String, String>().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        // Step 4: Create CashCard requests
        val cards = listOf(
            CashCardRequest(250.00),
            CashCardRequest(300.00)
        )

        // Create CashCards
        var createResponse: ResponseEntity<Void>? = null
        cards.forEach { card ->
            val httpEntity = HttpEntity(card, headers)
            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)
            assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)
        }

        // Step 5: Verify the last CashCard creation and get its URI
        val cardUri = createResponse?.headers?.location
        assertThat(cardUri).isNotNull

        // Step 6: Delete the last created CashCard
        val deleteResponse = restTemplate.exchange(cardUri, HttpMethod.DELETE, HttpEntity<Void>(headers), Void::class.java)
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        // Step 7: Attempt to get the deleted CashCard, expecting a 404 Not Found
//        val getResponse = restTemplate.exchange(cardUri, HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
//        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldNotDeleteACashCardThatDoesNotExist() {

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        // Verify registration is successful
        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)

        // Extract the JWT token from the registration response
        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")
        assertThat(token).isNotBlank

        // Set up authorization headers with the JWT token
        val headers = LinkedMultiValueMap<String, String>().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $token")
            add(HttpHeaders.CONTENT_TYPE, "application/json")
        }

        val deleteResponse = restTemplate
            .exchange("/cashcards/99999", HttpMethod.DELETE, HttpEntity<Void>(headers), Void::class.java)
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {

        val cards = listOf(
            CashCardRequest(250.00),
            CashCardRequest(300.00),
        )

        val registerResponse = restTemplate.postForEntity(
            "/auth/register", User(
                email = "user4@xyz.com",
                password = "Test@123",
                telephone = "09128383322",
                firstname = "User2",
                lastname = "Smith",
                role = Role.OWNER
            ), String::class.java
        )

        val documentResponse = JsonPath.parse(registerResponse.body)
        val token = documentResponse.read<String>("$.accessToken")

        println("registerResponse: ${registerResponse.body}")

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.OK)
        val headers = LinkedMultiValueMap<String, String>()

        headers.add(
            HttpHeaders.AUTHORIZATION,
            "Bearer $token"
        )
        headers.add(
            HttpHeaders.CONTENT_TYPE,
            "application/json"
        )

        var createResponse: ResponseEntity<Void>? = null
        var httpEntity: HttpEntity<CashCardRequest>? = null

        cards.forEach { card ->
            httpEntity = HttpEntity(card, headers)

            createResponse = restTemplate.exchange("/cashcards", HttpMethod.POST, httpEntity, Void::class.java)

        }
        assertThat(createResponse?.statusCode).isEqualTo(HttpStatus.CREATED)

        val emptyRequestEntity = HttpEntity(null, headers)

        val deleteResponse = restTemplate
            .exchange("/cashcards/3", HttpMethod.DELETE, emptyRequestEntity, Void::class.java)
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

//        val getResponse = restTemplate
//            .withBasicAuth("kumar2", "xyz789")
//            .getForEntity("/cashcards/102", String::class.java)
//        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

}