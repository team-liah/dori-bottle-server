package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
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
}