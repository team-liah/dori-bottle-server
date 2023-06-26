package com.liah.doribottle.config.security

import com.liah.doribottle.utils.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val secretKey: String
) : OncePerRequestFilter() {
    private val repository = RequestAttributeSecurityContextRepository()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (!token.isNullOrEmpty() && JwtUtil.validateToken(secretKey, token)) {
            val id = JwtUtil.getId(token, secretKey)
            val role = JwtUtil.getRole(token, secretKey)

            val authenticationToken = UsernamePasswordAuthenticationToken(id, null, listOf(SimpleGrantedAuthority(role)))

            authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
            SecurityContextHolder.getContext().authentication = authenticationToken
            repository.saveContext(SecurityContextHolder.getContext(), request, response)
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (!authorization.isNullOrEmpty() && authorization.startsWith("Bearer ")) {
            return authorization.substring(7)
        }

        val cookieToken = request.cookies?.firstOrNull { c -> c.name.equals("token") }?.value
        if (!cookieToken.isNullOrEmpty()) {
            return cookieToken
        }

        return null
    }
}