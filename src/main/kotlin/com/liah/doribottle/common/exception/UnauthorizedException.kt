package com.liah.doribottle.common.exception

class UnauthorizedException(message: String? = "인증되지 않은 요청입니다.") : RuntimeException(message)