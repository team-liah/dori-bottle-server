package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/payment"

    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository

    @AfterEach
    internal fun destroy() {
        paymentCategoryRepository.deleteAll()
    }

    @DisplayName("결제 카테고리 목록 조회")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun getAllCategories() {
        insertCategories()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectAmountsValue = listOf(10, 20, 30)
        val expectPriceValue = listOf(1000, 2000, 3000)
        val expectDiscountRateValue = listOf(10, 10, 10)
        val expectDiscountPriceValue = listOf(900, 1800, 2700)

        mockMvc.perform(
            get("${endPoint}/category")
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].amounts", `is`(expectAmountsValue)))
            .andExpect(jsonPath("content[*].price", `is`(expectPriceValue)))
            .andExpect(jsonPath("content[*].discountRate", `is`(expectDiscountRateValue)))
            .andExpect(jsonPath("content[*].discountPrice", `is`(expectDiscountPriceValue)))
    }

    private fun insertCategories() {
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(30, 3000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(40, 4000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(60, 6000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(20, 2000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(50, 5000, 10, after10Days, after10Days))
    }
}