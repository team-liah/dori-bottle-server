package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiredMs}") private val expiredMs: Long,
    @Value("\${jwt.preAuthExpiredMs}") private val preAuthExpiredMs: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun createPreAuthToken(doriUser: DoriUser): String {
        val now = Date()
        val expiredDate = Date(now.time + preAuthExpiredMs)
        return createToken(doriUser.id, doriUser.loginId, doriUser.role, now, expiredDate)
    }

    fun createToken(id: UUID, loginId: String, role: Role): String {
        val now = Date()
        val expiredDate = Date(now.time + expiredMs)
        return createToken(id, loginId, role, now, expiredDate)
    }

    private fun createToken(id: UUID, loginId: String, role: Role, issueDate: Date, expiredDate: Date): String {
        return Jwts.builder()
            .setClaims(mapOf(
                "sub" to id.toString(),
                "loginId" to loginId,
                "role" to role.key
            ))
            .setIssuedAt(issueDate)
            .setExpiration(expiredDate)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .compact()
    }

    fun getUserIdFromToken(token: String): UUID {
        val subject = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .subject

        return UUID.fromString(subject)
    }

    fun getUserLoginIdFromToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .get("loginId", String::class.java)
    }

    fun getUserRoleFromToken(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .build()
            .parseClaimsJws(token)
            .body
            .get("role", String::class.java)
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .build()
                .parseClaimsJws(authToken)
            return true
        } catch (ex: MalformedJwtException) {
            log.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            log.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            log.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            log.error("JWT claims string is empty.")
        }
        return false
    }
}