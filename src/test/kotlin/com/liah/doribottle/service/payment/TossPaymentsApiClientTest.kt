package com.liah.doribottle.service.payment

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class TossPaymentsApiClientTest {
    @Autowired
    private lateinit var tossPaymentsApiClient: TossPaymentsApiClient

    @DisplayName("빌링키 발급")
    @Test
    fun issueBillingKey() {
        //given
//        tossPaymentsApiClient.issueBillingKey("NAIn23FavoWKBnI5JDuUb", "Ikk8shAUtIdPZhawy6BZ2")

        //when

        //then
    }
}