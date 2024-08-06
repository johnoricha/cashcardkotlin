package com.example.cashcardkotlin

import com.example.cashcardkotlin.cashcard.models.CashCard
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester
import org.springframework.core.io.ClassPathResource

@JsonTest
class CashCardJsonTest {

    @Autowired
    private lateinit var json: JacksonTester<CashCard>

    @Autowired
    private lateinit var jsonList: JacksonTester<List<CashCard>>

    private lateinit var cashCards: List<CashCard>

    @BeforeEach
    fun setUp() {
        cashCards = listOf(
            CashCard(99L, 123.45, "sarah1"),
            CashCard(100L, 1.0, "sarah1"),
            CashCard(101L, 150.00, "sarah1"),
        )

    }

    @Test
    fun cashCardListSerializationTest() {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson(ClassPathResource("list.json"))
    }

    @Test
    fun cashCardSerializationTest() {
        val cashCard = cashCards[0]

        assertThat(json.write(cashCard)).isStrictlyEqualToJson(ClassPathResource("single.json"))
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
    }

    @Test
    fun cashCardListDeserializationTest() {
        val expected = """
                     [
                      {"id": 99, "amount": 123.45 , "owner": "sarah1"},
                      {"id": 100, "amount": 1.00 , "owner": "sarah1"},
                      {"id": 101, "amount": 150.00, "owner": "sarah1"}
                     ]
                """

        assertThat(jsonList.parse(expected))
                .isEqualTo(cashCards);
    }

    @Test
    fun cashCardDeserializationTest() {
        val expected = """
                {
                    "id":99,
                    "amount":123.45,
                    "owner": "sarah1"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(CashCard(99L, 123.45, "sarah1"));
        assertThat(json.parseObject(expected).id).isEqualTo(99);
        assertThat(json.parseObject(expected).amount).isEqualTo(123.45);
    }
}
