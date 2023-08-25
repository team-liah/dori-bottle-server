package com.liah.doribottle.common.error.exception

class PaymentCancelException(errorCode: ErrorCode = ErrorCode.PAYMENT_CANCEL_ERROR) : BusinessException(errorCode)