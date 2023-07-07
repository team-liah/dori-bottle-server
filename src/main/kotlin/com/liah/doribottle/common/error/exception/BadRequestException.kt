package com.liah.doribottle.common.error.exception

class BadRequestException(errorCode: ErrorCode) : BusinessException(errorCode)