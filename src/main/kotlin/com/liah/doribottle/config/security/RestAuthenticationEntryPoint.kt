package com.liah.doribottle.config.security

import com.liah.doribottle.common.exhandler.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AuthenticationException
    ) {
        val errorResponse = ErrorResponse(e.message, HttpStatus.UNAUTHORIZED.value())
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
//        response.writer?.write(errorResponse.convertJsonToString())
    }
}