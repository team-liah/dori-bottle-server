package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.findBy
import com.liah.doribottle.extension.systemId
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class TokenProvider(
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${app.auth.jwt.secret}") private val secret: String,
    @Value("\${app.auth.jwt.expiredMs}") private val expiredMs: Long,
    @Value("\${app.auth.jwt.preAuthExpiredMs}") private val preAuthExpiredMs: Long,
    @Value("\${app.auth.jwt.systemExpiredMs}") private val systemExpiredMs: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun preAuthAccessToken(doriUser: DoriUser): String {
        val now = Date()
        val expiredDate = Date(now.time + preAuthExpiredMs)
        return generateAccessToken(doriUser.id, doriUser.loginId, doriUser.name, doriUser.role, now, expiredDate)
    }

    fun generateAccessToken(id: UUID, loginId: String, name: String, role: Role): String {
        val now = Date()
        val expiredDate = Date(now.time + expiredMs)
        return generateAccessToken(id, loginId, name, role, now, expiredDate)
    }

    fun generateSystemAccessToken(loginId: String, name: String): String {
        val now = Date()
        val expiredDate = Date(now.time + systemExpiredMs)
        return generateAccessToken(systemId(), loginId, name, Role.SYSTEM, now, expiredDate)
    }

    private fun generateAccessToken(id: UUID, loginId: String, name: String, role: Role, issueDate: Date, expiredDate: Date): String {
        return Jwts.builder()
            .setClaims(mapOf(
                "sub" to id.toString(),
                "loginId" to loginId,
                "name" to name,
                "role" to role.key
            ))
            .setIssuedAt(issueDate)
            .setExpiration(expiredDate)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .compact()
    }

    fun extractDoriUserFromAccessToken(accessToken: String): DoriUser {
        val body = accessTokenClaims(accessToken)

        val id = UUID.fromString(body.subject)
        val loginId = getValueFromBody(body, "loginId")
        val name = getValueFromBody(body, "name")
        val role = (Role::key findBy getValueFromBody(body, "role"))!!
        return DoriUser(id, loginId, name, role)
    }

    fun extractUserIdFromAccessToken(accessToken: String): UUID {
        val subject = accessTokenClaims(accessToken).subject

        return UUID.fromString(subject)
    }

    fun extractUserLoginIdFromAccessToken(accessToken: String): String {
        val body = accessTokenClaims(accessToken)

        return getValueFromBody(body, "loginId")
    }

    fun extractUserRoleFromAccessToken(accessToken: String): String {
        val body = accessTokenClaims(accessToken)

        return getValueFromBody(body, "role")
    }

    private fun accessTokenClaims(accessToken: String) = Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
        .build()
        .parseClaimsJws(accessToken)
        .body

    private fun getValueFromBody(body: Claims, key: String) = body.get(key, String::class.java)

    fun validateAccessToken(authToken: String): Boolean {
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

    fun generateRefreshToken(userId: String) = refreshTokenRepository.save(RefreshToken(userId = userId)).refreshToken!!

    fun expireRefreshToken(refreshToken: String) = refreshTokenRepository.deleteById(refreshToken)

    fun getRefreshToken(refreshToken: String) = refreshTokenRepository.findByIdOrNull(refreshToken)
}