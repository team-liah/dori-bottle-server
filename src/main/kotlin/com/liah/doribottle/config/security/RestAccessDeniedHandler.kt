package com.liah.doribottle.config.security

import com.liah.doribottle.common.exhandler.ErrorResponse
import com.liah.doribottle.extension.convertJsonToString
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

class RestAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        e: AccessDeniedException
    ) {
        val errorResponse = ErrorResponse(e.message, HttpStatus.FORBIDDEN.value())
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.writer?.write(errorResponse.convertJsonToString())
    }
}