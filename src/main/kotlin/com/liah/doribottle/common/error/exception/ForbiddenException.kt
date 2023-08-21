package com.liah.doribottle.common.error.exception

class ForbiddenException(errorCode: ErrorCode = ErrorCode.ACCESS_DENIED) : BusinessException(errorCode)