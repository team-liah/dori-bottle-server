package com.liah.doribottle.config.security

import com.liah.doribottle.utils.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val secretKey: String
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (authorization.isNullOrEmpty() || !authorization.startsWith("Bearer ")) {
            log.error("Authorization is empty")
            filterChain.doFilter(request, response)
            return
        }

        val token = authorization.split(" ")[1]
        if (JwtUtil.isExpired(token, secretKey)) {
            log.error("Token is expired")
            filterChain.doFilter(request, response)
            return
        }

        val loginId = JwtUtil.getLoginId(token, secretKey)
        val role = JwtUtil.getRole(token, secretKey)

        val authenticationToken = UsernamePasswordAuthenticationToken(loginId, null, listOf(SimpleGrantedAuthority(role)))

        authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authenticationToken
        filterChain.doFilter(request, response)
    }
}