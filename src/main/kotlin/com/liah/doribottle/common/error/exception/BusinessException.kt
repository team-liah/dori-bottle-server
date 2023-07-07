package com.liah.doribottle.common.error.exception

open class BusinessException : RuntimeException {
    var errorCode: ErrorCode
        private set

    constructor(errorCode: ErrorCode) : super(errorCode.message) { this.errorCode = errorCode }
}