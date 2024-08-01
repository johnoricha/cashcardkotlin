package example.cashcardkotlin

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import com.example.cashcardkotlin.CashCard
import com.example.cashcardkotlin.CashCardKotlinApplication
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.*
import org.springframework.test.annotation.DirtiesContext

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [CashCardKotlinApplication::class]
)
@ComponentScan(basePackages = ["com.example.cashcardkotlin"])
@EnableAutoConfiguration
@Configuration
internal class CashCardApplicationTests {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldReturnACashCardWhenDataIsSaved() {
        val response: ResponseEntity<CashCard> = restTemplate
            .withBasicAuth("sarah1", "abc123").getForEntity("/cashcards/99", CashCard::class.java)

        val jsonString = objectMapper.writeValueAsString(response.body)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(jsonString)
        val id = documentContext.read<Number>("$.id")
        assertThat(id).isEqualTo(99)

        val amount = documentContext.read<Double>("$.amount")
        assertThat(amount).isEqualTo(123.45)
    }

    @Test
    @Throws(JsonProcessingException::class)
    fun shouldReturnACashCardWithAnUnknownId() {
        val response: ResponseEntity<CashCard> = restTemplate
            .withBasicAuth("sarah1", "abc123").getForEntity(
                "/cashcards/1000", CashCard::class.java
            )

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    @Throws(JsonProcessingException::class)
    fun shouldCreateANewCashCard() {
        val cashCard: CashCard = CashCard(null, 250.00, null)

        val createResponse = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .postForEntity("/cashcards", cashCard, Void::class.java)

        assertThat(createResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val locationOfCreatedCashCard = createResponse.headers.location

        val getResponse: ResponseEntity<CashCard> = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity(locationOfCreatedCashCard.toString(), CashCard::class.java)

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK)

        val jsonString = objectMapper.writeValueAsString(getResponse.getBody())

        val documentContext = JsonPath.parse(jsonString)
        val id = documentContext.read<Number>("$.id")
        val amount = documentContext.read<Double>("$.amount")

        assertThat(id).isNotNull()
        assertThat(amount).isEqualTo(250.00)
    }

    @Test
    @DirtiesContext
    fun shouldReturnAListCashCards() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards", String::class.java)


        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)

        val cashCardCount = documentContext.read<Int>("$.length()")
        assertThat(cashCardCount).isEqualTo(3)

        val ids = documentContext.read<JSONArray>("$..id")
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101)

        val amounts = documentContext.read<JSONArray>("$..amount")
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00)
    }

    @Test
    fun shouldReturnAPageOfCashCards() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards?page=0&size=1", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)
        val page = documentContext.read<JSONArray>("$[*]")
        assertThat(page.size).isEqualTo(1)
    }

    @Test
    fun shouldReturnASortedPageOfCashCards() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)

        val read = documentContext.read<JSONArray>("$[*]")
        assertThat(read.size).isEqualTo(1)

        val firstAmount = documentContext.read<Double>("$[0].amount")
        assertThat(firstAmount).isEqualTo(150.0)
    }

    @Test
    fun shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val documentContext = JsonPath.parse(response.body)
        val page = documentContext.read<JSONArray>("$[*]")
        assertThat(page.size).isEqualTo(3)

        val amounts = documentContext.read<JSONArray>("$..amount")
        assertThat(amounts).containsExactly(1.00, 123.45, 150.00)
    }

    @Test
    fun shouldNotReturnACashCardWhenUsingBadCredentials() {
        var response = restTemplate
            .withBasicAuth("BAD-USER", "abc123")
            .getForEntity("/cashcards/99", String::class.java)
        println("status code: " + response.statusCode)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)

        response = restTemplate
            .withBasicAuth("sarah1", "BAD-PASSWORD")
            .getForEntity("/cashcards/99", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun shouldRejectUsersWhoAreNotCardOwners() {
        val response = restTemplate
            .withBasicAuth("hank-owns-no-cards", "qrs456")
            .getForEntity("/cashcards/99", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun shouldNotAllowAccessToCashCardsTheyDoNotOwn() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards/102", String::class.java) // kumar2's data
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldUpdateAnExistingCashCard() {
        val cashCardUpdate = CashCard(null, 19.99, null)
        val request: HttpEntity<CashCard> = HttpEntity<CashCard>(cashCardUpdate)
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/99", HttpMethod.PUT, request, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val getResponse = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards/99", String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        val documentContext = JsonPath.parse(getResponse.body)
        val id = documentContext.read<Number>("$.id")
        val amount = documentContext.read<Double>("$.amount")
        assertThat(id).isEqualTo(99)
        assertThat(amount).isEqualTo(19.99)
    }

    @Test
    fun shouldNotUpdateACashCardThatDoesNotExist() {
        val unknownCard: CashCard = CashCard(null, 19.99, null)
        val request: HttpEntity<CashCard> = HttpEntity<CashCard>(unknownCard)
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/99999", HttpMethod.PUT, request, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldNotUpdateACashCardThatIsOwnedBySomeoneElse() {
        val kumarsCard: CashCard = CashCard(null, 333.33, null)
        val request: HttpEntity<CashCard> = HttpEntity<CashCard>(kumarsCard)
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/102", HttpMethod.PUT, request, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @DirtiesContext
    fun shouldDeleteAnExistingCashCard() {
        val response = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/99", HttpMethod.DELETE, null, Void::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)

        val getResponse = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .getForEntity("/cashcards/99", String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldNotDeleteACashCardThatDoesNotExist() {
        val deleteResponse = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/99999", HttpMethod.DELETE, null, Void::class.java)
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun shouldNotAllowDeletionOfCashCardsTheyDoNotOwn() {
        val deleteResponse = restTemplate
            .withBasicAuth("sarah1", "abc123")
            .exchange("/cashcards/102", HttpMethod.DELETE, null, Void::class.java)
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        val getResponse = restTemplate
            .withBasicAuth("kumar2", "xyz789")
            .getForEntity("/cashcards/102", String::class.java)
        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
    }

//    @Test
//    fun shouldRegisterNewUser() {
//
//        val registerResponse = restTemplate.postForEntity(
//            "/auth/register", Users(
//                email = "john@email.com", userPassword = "password"
//            ), Users::class.java, Void::class.java
//        )
//
//        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)
//    }

}