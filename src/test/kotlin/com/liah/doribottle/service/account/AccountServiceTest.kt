package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.ForbiddenException
import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.sqs.AwsSqsSender
import com.liah.doribottle.service.sqs.dto.PointSaveMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant

class AccountServiceTest : BaseServiceTest() {
    @Autowired private lateinit var accountService: AccountService
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var tokenProvider: TokenProvider

    @MockBean
    private lateinit var mockAwsSqsSender: AwsSqsSender

    private val loginId = "010-0000-0000"

    @DisplayName("회원가입")
    @Test
    fun register() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val saveUser = userRepository.save(User(loginId, "사용자", loginId, Role.GUEST))
        clear()

        //when
        accountService.register(saveUser.loginId, "Tester", "19970224", MALE, true, true, false)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(saveUser.id)

        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(findUser?.phoneNumber).isEqualTo(loginId)
        assertThat(findUser?.role).isEqualTo(Role.USER)
        assertThat(findUser?.birthDate).isEqualTo("19970224")
        assertThat(findUser?.gender).isEqualTo(MALE)
        assertThat(findUser?.agreedTermsOfServiceDate).isNotNull
        assertThat(findUser?.agreedTermsOfServiceDate).isNotNull
        assertThat(findUser?.agreedTermsOfMarketingDate).isNull()

        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("비밀번호 업데이트")
    @Test
    fun updatePassword() {
        //given
        val loginPassword = "123456"

        //when
        val id = accountService.updatePassword(loginId, loginPassword)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(id)
        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(findUser?.phoneNumber).isEqualTo(loginId)
        assertThat(passwordEncoder.matches(loginPassword, findUser?.loginPassword)).isTrue
        assertThat(findUser?.role).isEqualTo(Role.GUEST)
        assertThat(findUser?.loginExpirationDate).isAfter(Instant.now())
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        saveUser.updatePassword(encryptedPassword)
        clear()

        //when
        val authDto = accountService.auth(loginId, loginPassword)
        clear()

        //then
        assertThat(tokenProvider.validateAccessToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.extractUserIdFromAccessToken(authDto.accessToken)).isEqualTo(saveUser.id)
        assertThat(tokenProvider.extractUserRoleFromAccessToken(authDto.accessToken)).isEqualTo("ROLE_USER")
        assertThat(authDto.refreshToken).isNotNull
    }

    @DisplayName("인증 예외")
    @Test
    fun authException() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        saveUser.updatePassword(encryptedPassword)
        clear()

        // TODO: DisabledException, LockedException
        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            accountService.auth(loginId, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("Invalid login password.")
    }

    @DisplayName("재인증")
    @Test
    fun refreshAuth() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val saveRefreshToken = refreshTokenRepository.save(RefreshToken(userId = saveUser.id.toString()))
        clear()

        //when
        val authDto = accountService.refreshAuth(saveRefreshToken.refreshToken)
        clear()

        //then
        assertThat(tokenProvider.validateAccessToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.extractUserIdFromAccessToken(authDto.accessToken)).isEqualTo(saveUser.id)
        assertThat(tokenProvider.extractUserRoleFromAccessToken(authDto.accessToken)).isEqualTo("ROLE_USER")
        assertThat(saveRefreshToken.refreshToken).isNotEqualTo(authDto.refreshToken)
    }

    @DisplayName("인증 토큰")
    @Test
    fun preAuth() {
        //given
        val user = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val card = Card(CardProvider.HYUNDAI, CardProvider.HYUNDAI, "1234", CardType.CREDIT, CardOwnerType.PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, "key", PaymentMethodProviderType.TOSS_PAYMENTS, PaymentMethodType.CARD, card, true, Instant.now()))
        val doriUser = DoriUser(user.id, user.loginId, user.name, user.role)

        //when
        val accessToken = accountService.preAuth(doriUser)

        //then
        assertThat(tokenProvider.validateAccessToken(accessToken)).isTrue
        assertThat(tokenProvider.extractUserIdFromAccessToken(accessToken)).isEqualTo(user.id)
        assertThat(tokenProvider.extractUserRoleFromAccessToken(accessToken)).isEqualTo("ROLE_USER")
    }

    @DisplayName("인증 토큰 예외")
    @Test
    fun preAuthException() {
        //when, then
        val exception = assertThrows<ForbiddenException> {
            //given
            val user = User("010-0001-0001", "Tester 1", "010-0001-0001", Role.USER)
            user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
            userRepository.save(user)
            clear()

            val doriUser = DoriUser(user.id, loginId, "Tester", Role.USER)
            accountService.preAuth(doriUser)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.BLOCKED_USER_ACCESS_DENIED)
    }

    @DisplayName("유저 비활성화")
    @Test
    fun deactivate() {
        //given
        val user = userRepository.save(User(loginId, "사용자", loginId, Role.USER))
        clear()

        //when
        accountService.deactivate(user.id)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.active).isFalse()
    }
}