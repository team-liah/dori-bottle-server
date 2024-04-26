package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.domain.payment.PaymentType
import org.springframework.stereotype.Service
import java.util.*

@Service
class TosspaymentsService(
    private val tosspaymentsApiClient: TosspaymentsApiClient
) {
    fun issueBillingKey(
        authKey: String,
        userId: UUID
    ) = tosspaymentsApiClient.issueBillingKey(
        authKey = authKey,
        customerKey = userId.toString()
    )?.toBillingInfo() ?: throw BillingKeyIssuanceException()

    fun executeBilling(
        billingKey: String,
        userId: UUID,
        price: Long,
        paymentId: UUID,
        paymentType: PaymentType
    ) = tosspaymentsApiClient.executeBilling(
        billingKey = billingKey,
        customerKey = userId.toString(),
        amount = price,
        orderId = paymentId.toString(),
        orderName = paymentType.title
    )?.toPaymentResultDto() ?: throw BillingExecuteException()

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ) = tosspaymentsApiClient.cancelPayment(
        paymentKey = paymentKey,
        cancelReason = cancelReason
    )?.toPaymentResultDto() ?: throw PaymentCancelException()
}