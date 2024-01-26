package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.ForbiddenException
import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.LoginIdChange
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.LoginIdChangeRepository
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
import org.springframework.security.authentication.DisabledException
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.util.*

class AccountServiceTest : BaseServiceTest() {
    @Autowired private lateinit var accountService: AccountService
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired private lateinit var machineRepository: MachineRepository
    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var cupRepository: CupRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var loginIdChangeRepository: LoginIdChangeRepository
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
    fun saveOrUpdatePassword() {
        //given
        val loginPassword = "123456"

        //when
        val id = accountService.saveOrUpdatePassword(loginId, loginPassword)
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
        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            //given
            val user = User(loginId, "Tester", loginId, Role.USER)
            val loginPassword = "123456"
            val encryptedPassword = passwordEncoder.encode(loginPassword)
            user.updatePassword(encryptedPassword)
            userRepository.save(user)
            clear()

            accountService.auth(loginId, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("Invalid login password.")

        val disabledException = assertThrows<DisabledException> {
            //given
            val user = User("010-0001-0001", "Tester", "010-0001-0001", Role.USER)
            val loginPassword = "123456"
            val encryptedPassword = passwordEncoder.encode(loginPassword)
            user.updatePassword(encryptedPassword)
            user.inactivate(null)
            userRepository.save(user)

            accountService.auth("010-0001-0001", "123456")
        }
        assertThat(disabledException.message).isEqualTo("Account is disabled.")
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
        assertThat(exception.errorCode).isEqualTo(ErrorCode.BLOCKED_USER_ACCESS_DENIED_LOST_CUP)
    }

    @DisplayName("유저 비활성화")
    @Test
    fun inactivate() {
        //given
        val user = userRepository.save(User(loginId, "사용자", loginId, Role.USER))
        clear()

        //when
        accountService.inactivate(user.id, "테스트")
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.active).isFalse()
        assertThat(findUser?.inactivateReason).isEqualTo("테스트")
    }

    @DisplayName("유저 비활성화 예외")
    @Test
    fun inactivateException() {
        //given
        val user = userRepository.save(User(loginId, "사용자", loginId, Role.USER))
        val vendingMachine = machineRepository.save(Machine(MACHINE_NO1, MACHINE_NAME, MachineType.VENDING, Address("12345", "test"), Location(37.508855, 127.059479), 100, null))
        val cup = cupRepository.save(Cup(CUP_RFID))
        val rental = Rental(user, vendingMachine, true, 24)
        rental.confirm(cup)
        rentalRepository.save(rental)
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            accountService.inactivate(user.id, null)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.USER_INACTIVATE_NOT_ALLOWED)
    }

    @DisplayName("로그인 아이디 변경 요청 생성")
    @Test
    fun createLoginIdChange() {
        //given
        val userId = UUID.randomUUID()
        val toLoginId = "010-0000-0000"
        val authCode = "123456"

        //when
        accountService.createLoginIdChange(userId, toLoginId, authCode)

        //then
        val findLoginIdChangeRequest = loginIdChangeRepository.findByIdOrNull(userId.toString())
        assertThat(findLoginIdChangeRequest?.userId).isEqualTo(userId.toString())
        assertThat(findLoginIdChangeRequest?.toLoginId).isEqualTo("010-0000-0000")
        assertThat(findLoginIdChangeRequest?.authCode).isEqualTo("123456")
    }

    @DisplayName("로그인 아이디 변경 요청 생성 TC2")
    @Test
    fun createLoginIdChangeTc2() {
        //given
        val userId = UUID.randomUUID()
        val toLoginId = "010-0000-0000"
        val authCode1 = "123456"
        val authCode2 = "456789"

        //when
        accountService.createLoginIdChange(userId, toLoginId, authCode1)
        accountService.createLoginIdChange(userId, toLoginId, authCode2)

        //then
        val findLoginIdChangeRequest = loginIdChangeRepository.findByIdOrNull(userId.toString())
        assertThat(findLoginIdChangeRequest?.userId).isEqualTo(userId.toString())
        assertThat(findLoginIdChangeRequest?.toLoginId).isEqualTo("010-0000-0000")
        assertThat(findLoginIdChangeRequest?.authCode).isEqualTo(authCode2)
    }

    @DisplayName("로그인 아이디 변경 요청 생성 TC3")
    @Test
    fun createLoginIdChangeTc3() {
        //given
        val guest = userRepository.save(User(loginId, "Tester", loginId, Role.GUEST))
        val userId = UUID.randomUUID()
        val authCode = "123456"
        clear()

        //when
        accountService.createLoginIdChange(userId, loginId, authCode)

        //then
        val findLoginIdChangeRequest = loginIdChangeRepository.findByIdOrNull(userId.toString())
        val findGuest = userRepository.findByIdOrNull(guest.id)

        assertThat(findLoginIdChangeRequest?.userId).isEqualTo(userId.toString())
        assertThat(findLoginIdChangeRequest?.toLoginId).isEqualTo(loginId)
        assertThat(findLoginIdChangeRequest?.authCode).isEqualTo(authCode)

        assertThat(findGuest).isNull()
    }

    @DisplayName("로그인 아이디 변경 요청 생성 예외")
    @Test
    fun createLoginIdChangeException() {
        //given
        userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val userId = UUID.randomUUID()
        val authCode = "123456"
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            accountService.createLoginIdChange(userId, loginId, authCode)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.USER_ALREADY_REGISTERED)
    }

    @DisplayName("로그인 아이디 변경")
    @Test
    fun changeLoginId() {
        //given
        val user = userRepository.save(User("010-1111-1111", "Tester", "010-1111-1111", Role.USER))
        val authCode = "123456"
        loginIdChangeRepository.save(LoginIdChange(user.id.toString(), 300, "010-0000-0000", authCode))
        clear()

        //when
        accountService.changeLoginId(user.id, authCode)
        clear()

        val findUser = userRepository.findByIdOrNull(user.id)
        val findLoginIdChangeRequest = loginIdChangeRepository.findByIdOrNull(user.id.toString())

        assertThat(findUser?.loginId).isEqualTo("010-0000-0000")
        assertThat(findLoginIdChangeRequest).isNull()
    }

    @DisplayName("로그인 아이디 변경 예외")
    @Test
    fun changeLoginIdException() {
        //given
        val user = userRepository.save(User("010-1111-1111", "Tester", "010-1111-1111", Role.USER))
        val authCode = "123456"
        loginIdChangeRepository.save(LoginIdChange(user.id.toString(), 300, "010-0000-0000", authCode))
        clear()

        //when
        val exception = assertThrows<BusinessException> {
            accountService.changeLoginId(user.id, "000000")
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.LOGIN_ID_NOT_ALLOWED)
    }
}