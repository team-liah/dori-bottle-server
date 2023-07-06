package com.liah.doribottle.common.exception

class BadRequestException(errorCode: ErrorCode) : BusinessException(errorCode)