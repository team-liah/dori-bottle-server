package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.repository.payment.PaymentCategoryQueryRepository
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.PaymentCategoryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class PaymentService(
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentCategoryRepository: PaymentCategoryRepository,
    private val paymentCategoryQueryRepository: PaymentCategoryQueryRepository,
    private val userRepository: UserRepository
) {
    //TODO: Test
    fun registerMethod(
        userId: UUID,
        billingInfo: BillingInfo
    ): UUID {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val defaultMethod = paymentMethodRepository.findFirstByUserIdAndDefaultIsTrue(userId)

        val method = paymentMethodRepository.save(
            PaymentMethod(
                user = user,
                billingKey = billingInfo.billingKey,
                providerType = billingInfo.providerType,
                type = billingInfo.type,
                card = billingInfo.cardDto.toEmbeddable(),
                authenticatedDate = billingInfo.authenticatedDate,
                default = defaultMethod == null
            )
        )

        return method.id
    }

    fun registerCategory(
        amounts: Long,
        price: Long,
        discountRate: Int,
        discountExpiredDate: Instant?,
        expiredDate: Instant?
    ): UUID {
        val category = paymentCategoryRepository.save(
            PaymentCategory(
                amounts = amounts,
                price = price,
                discountRate = discountRate,
                discountExpiredDate = discountExpiredDate,
                expiredDate = expiredDate
            )
        )

        return category.id
    }

    @Transactional(readOnly = true)
    fun getCategory(
        categoryId: UUID
    ): PaymentCategoryDto {
        val category = paymentCategoryRepository.findByIdOrNull(categoryId)
            ?: throw NotFoundException(ErrorCode.PAYMENT_CATEGORY_NOT_FOUND)

        return category.toDto()
    }

    @Transactional(readOnly = true)
    fun getAllCategories(
        expired: Boolean? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<PaymentCategoryDto> {
        return paymentCategoryQueryRepository.getAll(
            expired = expired,
            deleted = deleted,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun removeCategory(
        categoryId: UUID
    ) {
        val category = paymentCategoryRepository.findByIdOrNull(categoryId)
            ?: throw NotFoundException(ErrorCode.PAYMENT_CATEGORY_NOT_FOUND)

        category.delete()
    }
}