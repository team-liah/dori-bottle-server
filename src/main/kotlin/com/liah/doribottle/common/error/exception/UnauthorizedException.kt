package com.liah.doribottle.common.error.exception

class UnauthorizedException(errorCode: ErrorCode = ErrorCode.UNAUTHORIZED) : BusinessException(errorCode)