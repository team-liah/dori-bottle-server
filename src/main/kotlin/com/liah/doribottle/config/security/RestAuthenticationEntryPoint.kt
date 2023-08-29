package com.liah.doribottle.config.security

import com.liah.doribottle.common.error.ErrorResponse
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.extension.expireCookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class RestAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        e: AuthenticationException
    ) {
        val errorResponse = ErrorResponse.of(ErrorCode.UNAUTHORIZED)
        val expiredAccessTokenCookie = expireCookie(
            url = request.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        response.setHeader(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer?.write(errorResponse.convertAnyToString())
    }
}