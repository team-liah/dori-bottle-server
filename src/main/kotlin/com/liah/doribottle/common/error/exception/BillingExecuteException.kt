package com.liah.doribottle.common.error.exception

class BillingExecuteException(errorCode: ErrorCode = ErrorCode.BILLING_EXECUTE_ERROR) : BusinessException(errorCode)