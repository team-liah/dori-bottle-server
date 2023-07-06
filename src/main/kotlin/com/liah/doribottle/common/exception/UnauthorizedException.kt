package com.liah.doribottle.common.exception

class UnauthorizedException(errorCode: ErrorCode = ErrorCode.UNAUTHORIZED) : BusinessException(errorCode)