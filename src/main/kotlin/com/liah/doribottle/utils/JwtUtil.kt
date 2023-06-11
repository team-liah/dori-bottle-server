package com.liah.doribottle.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.util.*

class JwtUtil {
    companion object {
        fun createJwt(
            loginId: String,
            role: String,
            secretKey: String,
            expiredMs: Long
        ): String = Jwts.builder()
            .setClaims(mapOf(
                "loginId" to loginId,
                "role" to role
            ))
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + expiredMs))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
            .compact()

        fun isExpired(
            token: String,
            secretKey: String
        ) = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
            .build()
            .parseClaimsJws(token)
            .body
            .expiration
            .before(Date())

        fun getLoginId(
            token: String,
            secretKey: String
        ): String = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
            .build()
            .parseClaimsJws(token)
            .body
            .get("loginId", String::class.java)

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