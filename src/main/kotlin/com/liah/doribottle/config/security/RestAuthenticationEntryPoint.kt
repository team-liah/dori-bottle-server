package com.liah.doribottle.config.security

import com.liah.doribottle.common.error.ErrorResponse
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.extension.convertJsonToString
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AuthenticationException
    ) {
        val errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer?.write(errorResponse.convertJsonToString())
    }
}