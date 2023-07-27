package com.liah.doribottle.service.payment

import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var paymentService: PaymentService
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository

    @DisplayName("결제 카테고리 등록")
    @Test
    fun registerCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)

        //when
        val id = paymentService.registerCategory(10, 1000, 10, after10Days, after10Days)
        clear()

        //then
        val findCategory = paymentCategoryRepository.findByIdOrNull(id)

        assertThat(findCategory?.amounts).isEqualTo(10)
        assertThat(findCategory?.price).isEqualTo(1000)
        assertThat(findCategory?.discountRate).isEqualTo(10)
        assertThat(findCategory?.discountExpiredDate).isEqualTo(after10Days)
        assertThat(findCategory?.expiredDate).isEqualTo(after10Days)
    }

    @DisplayName("결제 카테고리 상세 조회")
    @Test
    fun getCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(
            PaymentCategory(10, 1000, 10, after10Days, after10Days)
        )
        clear()

        //when
        val categoryDto = paymentService.getCategory(category.id)

        //then
        assertThat(categoryDto.amounts).isEqualTo(10)
        assertThat(categoryDto.price).isEqualTo(1000)
        assertThat(categoryDto.discountRate).isEqualTo(10)
        assertThat(categoryDto.discountExpiredDate).isEqualTo(after10Days)
        assertThat(categoryDto.expiredDate).isEqualTo(after10Days)
    }

    @DisplayName("결제 카테고리 목록 조회")
    @Test
    fun getAllCategories() {
        //given
        insertCategories()
        clear()

        //when
        val result = paymentService.getAllCategories(
            expired = false,
            deleted = false,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("amounts")
            .containsExactly(10L, 20L, 30L)
        assertThat(result)
            .extracting("price")
            .containsExactly(1000L, 2000L, 3000L)
    }

    @DisplayName("결제 카테고리 상세 조회")
    @Test
    fun removeCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(
            PaymentCategory(10, 1000, 10, after10Days, after10Days)
        )
        clear()

        //when
        paymentService.removeCategory(category.id)
        clear()

        //then
        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)

        assertThat(findCategory?.deleted).isEqualTo(true)
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