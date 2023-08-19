package com.liah.doribottle.common.error.exception

class BillingKeyIssuanceException(errorCode: ErrorCode = ErrorCode.BILLING_KEY_ISSUANCE_ERROR) : BusinessException(errorCode)