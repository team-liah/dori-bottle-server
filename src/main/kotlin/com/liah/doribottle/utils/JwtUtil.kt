package com.liah.doribottle.utils

import com.liah.doribottle.domain.user.Role
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import java.util.*

class JwtUtil {
    companion object {
        private val log = LoggerFactory.getLogger(javaClass)
        fun createJwt(
            id: UUID,
            role: Role,
            secretKey: String,
            expiredMs: Long
        ): String {
            val now = Date()
            val expiredDate = Date(now.time + expiredMs)
            return Jwts.builder()
                .setClaims(mapOf(
                    "sub" to id.toString(),
                    "role" to role.key
                ))
                .setIssuedAt(now)
                .setExpiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact()
        }

        fun validateToken(
            secretKey: String,
            token: String
        ): Boolean {
            try {
                Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                    .build()
                    .parseClaimsJws(token)
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

        fun getId(
            token: String,
            secretKey: String
        ): UUID {
            val sub = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .body
                .subject

            return UUID.fromString(sub)
        }

        fun getRole(
            token: String,
            secretKey: String
        ): String = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
            .build()
            .parseClaimsJws(token)
            .body
            .get("role", String::class.java)
    }
}