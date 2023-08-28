package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.payment.*
import com.liah.doribottle.domain.payment.PaymentStatus.CANCELED
import com.liah.doribottle.domain.payment.PaymentType.SAVE_POINT
import com.liah.doribottle.repository.payment.*
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.payment.dto.*
import com.liah.doribottle.service.point.PointService
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
    private val paymentRepository: PaymentRepository,
    private val paymentQueryRepository: PaymentQueryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val paymentMethodQueryRepository: PaymentMethodQueryRepository,
    private val paymentCategoryRepository: PaymentCategoryRepository,
    private val paymentCategoryQueryRepository: PaymentCategoryQueryRepository,
    private val userRepository: UserRepository,
    private val pointRepository: PointRepository,
    private val pointService: PointService
) {
    fun create(
        userId: UUID,
        price: Long,
        type: PaymentType,
        card: CardDto
    ): UUID {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        val payment = paymentRepository.save(Payment(user, price, type, card.toEmbeddable()))

        return payment.id
    }

    @Transactional(readOnly = true)
    fun get(
        id: UUID
    ): PaymentDto {
        val payment = paymentRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.PAYMENT_NOT_FOUND)

        return payment.toDto()
    }

    @Transactional(readOnly = true)
    fun getAll(
        userId: UUID? = null,
        type: PaymentType? = null,
        statuses: Set<PaymentStatus>? = null,
        pageable: Pageable
    ): Page<PaymentDto> {
        return paymentQueryRepository.getAll(
            userId = userId,
            type = type,
            statuses = statuses,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun updateResult(
        id: UUID,
        result: PaymentResultDto?,
        pointId: UUID? = null
    ) {
        val payment = paymentRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.PAYMENT_NOT_FOUND)
        val point = pointId?.let { pointRepository.findByIdOrNull(pointId) }

        payment.updateResult(result?.toEmbeddable(), point)

        if (payment.status == CANCELED && payment.type == SAVE_POINT) {
            point?.let { pointService.expire(it.id, it.userId) }
        }
    }

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

    @Transactional(readOnly = true)
    fun getMethod(
        id: UUID
    ): PaymentMethodDto {
        val method = paymentMethodRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)

        return method.toDto()
    }

    @Transactional(readOnly = true)
    fun getDefaultMethod(
        userId: UUID
    ): PaymentMethodDto {
        val method = paymentMethodRepository.findFirstByUserIdAndDefaultIsTrue(userId)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)

        return method.toDto()
    }

    @Transactional(readOnly = true)
    fun getAllMethods(
        userId: UUID,
        pageable: Pageable
    ): Page<PaymentMethodDto> {
        return paymentMethodQueryRepository.getAll(
            userId = userId,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun changeDefaultMethod(
        id: UUID,
        userId: UUID
    ) {
        val originDefaultMethod = paymentMethodRepository.findFirstByUserIdAndDefaultIsTrue(userId)
        val newDefaultMethod = paymentMethodRepository.findByIdAndUserId(id, userId)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)

        originDefaultMethod?.update(default = false)
        newDefaultMethod.update(default = true)
    }

    fun removeMethod(
        id: UUID
    ) {
        val method = paymentMethodRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)
        if (method.default) throw BusinessException(ErrorCode.PAYMENT_METHOD_REMOVE_NOT_ALLOWED)

        paymentMethodRepository.delete(method)
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