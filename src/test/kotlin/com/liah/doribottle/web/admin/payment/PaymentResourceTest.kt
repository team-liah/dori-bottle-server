package com.liah.doribottle.web.admin.payment

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategoryRegisterOrUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/payment"

    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository

    @DisplayName("결제 카테고리 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun registerCategory() {
        val body = PaymentCategoryRegisterOrUpdateRequest(10, 1000, 10, null, null)

        mockMvc.perform(
            post("${endPoint}/category")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("결제 카테고리 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAllCategories() {
        insertCategories()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectAmountsValue = listOf(60, 50, 40)
        val expectPriceValue = listOf(6000, 5000, 4000)
        val expectDiscountRateValue = listOf(10, 10, 10)
        val expectDiscountPriceValue = listOf(5400, 4500, 3600)

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

    @DisplayName("결제 카테고리 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun updateCategory() {
        //given
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, null, null))
        val body = PaymentCategoryRegisterOrUpdateRequest(20, 2000, 20, null, null)

        //when, then
        mockMvc.perform(
            put("${endPoint}/category/${category.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)
        assertThat(findCategory?.amounts).isEqualTo(20)
        assertThat(findCategory?.price).isEqualTo(2000)
        assertThat(findCategory?.discountRate).isEqualTo(20)
    }

    @DisplayName("결제 카테고리 제거")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun removeCategory() {
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, null, null))

        mockMvc.perform(
            delete("${endPoint}/category/${category.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)
        assertThat(findCategory).isNull()
    }

    private fun insertCategories() {
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(20, 2000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(30, 3000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(40, 4000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(50, 5000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(60, 6000, 10, after10Days, after10Days))
    }
}