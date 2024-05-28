package com.liah.doribottle.service.payment

import com.liah.doribottle.apiclient.TosspaymentsApiClient
import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.PaymentResultDto
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PaymentGatewayService(
    private val tosspaymentsApiClient: TosspaymentsApiClient,
) {
    fun issueBillingKey(
        authKey: String,
        userId: UUID,
    ): BillingInfo {
        return kotlin.runCatching {
            tosspaymentsApiClient.issueBillingKey(
                authKey = authKey,
                customerKey = userId.toString(),
            )
        }.getOrNull()?.toBillingInfo()
            ?: throw BillingKeyIssuanceException()
    }

    fun executeBilling(
        billingKey: String,
        userId: UUID,
        price: Long,
        paymentId: UUID,
        paymentType: PaymentType,
    ): PaymentResultDto {
        return kotlin.runCatching {
            tosspaymentsApiClient.executeBilling(
                billingKey = billingKey,
                customerKey = userId.toString(),
                amount = price,
                orderId = paymentId.toString(),
                orderName = paymentType.title,
            )
        }.getOrNull()?.toPaymentResultDto()
            ?: throw BillingExecuteException()
    }

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String,
    ): PaymentResultDto {
        return kotlin.runCatching {
            tosspaymentsApiClient.cancelPayment(
                paymentKey = paymentKey,
                cancelReason = cancelReason,
            )
        }.getOrNull()?.toPaymentResultDto()
            ?: throw PaymentCancelException()
    }
}
