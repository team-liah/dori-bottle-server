package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.utils.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,

    @Value("\${jwt.secret}") val secretKey: String,
    @Value("\${jwt.expiredMs}") val expiredMs: Long
) : UserDetailsService {
    override fun loadUserByUsername(loginId: String): UserDetails {
        return IdentityUser.fromUser(getUserByLoginId(loginId))
    }

    fun auth(
        loginId: String,
        loginPassword: String
    ): String {
        val user = getUserByLoginId(loginId)

        // TODO: throw if expired password
        if (loginPassword != user.loginPassword) throw BadCredentialsException("")
        if (!user.active) throw DisabledException("")
        if (user.blocked) throw LockedException("")

        return JwtUtil.createJwt(loginId, user.role.name, secretKey, expiredMs)
    }

    /**
     * Join user
     *
     * @param phoneNumber for loginId
     * @param name
     * @param role
     * @return result user's id
     * @throws IllegalArgumentException if duplicate phoneNumber(loginId)
     */
    @Transactional
    fun join(
        phoneNumber: String,
        name: String,
        role: Role
    ): UUID {
        verifyDuplicateLoginId(phoneNumber)
        val user = userRepository.save(
            User(
                loginId = phoneNumber,
                name = name,
                phoneNumber = phoneNumber,
                role = role
            )
        )

        return user.id
    }

    private fun getUserByLoginId(loginId: String) = userRepository
        .findByLoginId(loginId)
        .orElseThrow { UsernameNotFoundException("User $loginId was not found in the database) }") }

    /**
     * Verify user's loginId is duplicated
     *
     * @param loginId
     * @throws IllegalArgumentException if duplicate loginId
     */
    private fun verifyDuplicateLoginId(
        loginId: String
    ) {
        if (userRepository.findByLoginId(loginId).isPresent) {
            throw IllegalArgumentException("이미 존재하는 회원입니다.")
        }
    }
}