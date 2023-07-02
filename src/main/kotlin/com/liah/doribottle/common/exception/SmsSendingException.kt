package com.liah.doribottle.common.exception

class SmsSendingException(message: String? = "SMS 발송 실패했습니다.") : RuntimeException(message)