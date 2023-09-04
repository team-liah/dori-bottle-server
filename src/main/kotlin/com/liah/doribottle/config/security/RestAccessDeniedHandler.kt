package com.liah.doribottle.config.security

import com.liah.doribottle.common.error.ErrorResponse
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.extension.convertAnyToString
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class RestAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        e: AccessDeniedException
    ) {
        val errorResponse = ErrorResponse.of(ErrorCode.ACCESS_DENIED)
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.writer?.write(errorResponse.convertAnyToString())
    }
}