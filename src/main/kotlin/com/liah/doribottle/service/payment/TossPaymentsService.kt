package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.domain.payment.PaymentType
import org.springframework.stereotype.Service
import java.util.*

@Service
class TossPaymentsService(
    private val tossPaymentsApiClient: TossPaymentsApiClient
) {
    fun issueBillingKey(
        authKey: String,
        userId: UUID
    ) = tossPaymentsApiClient.issueBillingKey(
        authKey = authKey,
        customerKey = userId.toString()
    )?.toBillingInfo() ?: throw BillingKeyIssuanceException()

    fun executeBilling(
        billingKey: String,
        userId: UUID,
        price: Long,
        paymentId: UUID,
        paymentType: PaymentType
    ) = tossPaymentsApiClient.executeBilling(
        billingKey = billingKey,
        customerKey = userId.toString(),
        amount = price,
        orderId = paymentId.toString(),
        orderName = paymentType.title
    )?.toPaymentResultDto() ?: throw BillingExecuteException()
}