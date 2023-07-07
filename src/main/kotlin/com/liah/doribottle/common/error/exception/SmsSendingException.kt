package com.liah.doribottle.common.error.exception

class SmsSendingException(errorCode: ErrorCode = ErrorCode.SMS_SENDING_ERROR) : BusinessException(errorCode)