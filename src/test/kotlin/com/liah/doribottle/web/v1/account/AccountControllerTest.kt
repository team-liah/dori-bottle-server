package com.liah.doribottle.web.v1.account

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.LoginIdChangeRequest
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.inquiry.InquiryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.user.LoginIdChangeRequestRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.service.sqs.AwsSqsSender
import com.liah.doribottle.service.sqs.dto.PointSaveMessage
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.account.vm.*
import jakarta.servlet.http.Cookie
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant
import java.util.*

class AccountControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/account"

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var inquiryRepository: InquiryRepository
    @Autowired private lateinit var loginIdChangeRequestRepository: LoginIdChangeRequestRepository

    @MockBean
    private lateinit var mockSmsService: SmsService
    @MockBean
    private lateinit var mockAwsSqsSender: AwsSqsSender

    private lateinit var user: User
    private lateinit var guest: User

    private lateinit var userRefreshToken: RefreshToken
    private lateinit var guestRefreshToken: RefreshToken

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.updatePassword(encodePassword("123456"))
        user = userRepository.save(userEntity)
        userRefreshToken = refreshTokenRepository.save(RefreshToken(userId = user.id.toString()))

        guest = userRepository.save(User(GUEST_LOGIN_ID, "사용자", GUEST_LOGIN_ID, Role.GUEST))
        guestRefreshToken = refreshTokenRepository.save(RefreshToken(userId = guest.id.toString()))
    }

    @AfterEach
    internal fun destroy() {
        inquiryRepository.deleteAll()
        pointRepository.deleteAll()
        paymentMethodRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("인증요청")
    @Test
    fun sendAuthSms() {
        doNothing().`when`(mockSmsService).sendLoginAuthSms(any<String>(), any<String>())
        val body = SendSmsRequest(USER_LOGIN_ID)

        mockMvc.perform(
            post("$endPoint/auth/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        verify(mockSmsService, times(1)).sendLoginAuthSms(any<String>(), any<String>())
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        val body = AuthRequest(USER_LOGIN_ID, "123456")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 - Unauthorized")
    @Test
    fun authException() {
        val body = AuthRequest(USER_LOGIN_ID, "000000")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
    }

    @DisplayName("인증 새로고침")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun refreshAuth() {
        val cookie = Cookie(REFRESH_TOKEN, userRefreshToken.refreshToken)

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 새로고침 - Unauthorized")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun refreshAuthException() {
        val cookie = Cookie(REFRESH_TOKEN, UUID.randomUUID().toString())

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
    }

    @DisplayName("Dori User Pre Auth Token")
    @Test
    fun getPreAuthToken() {
        //given
        val card = Card(CardProvider.HYUNDAI, CardProvider.HYUNDAI, "1234", CardType.CREDIT, CardOwnerType.PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, "key", PaymentMethodProviderType.TOSS_PAYMENTS, PaymentMethodType.CARD, card, true, Instant.now()))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            get("$endPoint/pre-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("accessToken", notNullValue()))
    }

    @DisplayName("Dori User Pre Auth Token Exception")
    @Test
    fun getPreAuthTokenException() {
        val user = User("010-1234-1234", "Tester", "010-1234-1234", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get("$endPoint/pre-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("code", `is`(ErrorCode.BLOCKED_USER_ACCESS_DENIED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.BLOCKED_USER_ACCESS_DENIED.message)))
    }

    @DisplayName("Dori User Pre Auth Token Exception TC2")
    @Test
    fun getPreAuthTokenExceptionTc2() {
        userRepository.save(User("010-1234-1234", "Tester", "010-1234-1234", Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get("$endPoint/pre-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("code", `is`(ErrorCode.PAYMENT_METHOD_NOT_FOUND.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.PAYMENT_METHOD_NOT_FOUND.message)))
    }

    @DisplayName("로그아웃")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun logout() {
        val cookie = Cookie(REFRESH_TOKEN, userRefreshToken.refreshToken)

        mockMvc.perform(
            post("$endPoint/logout")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(cookie().value(REFRESH_TOKEN, emptyOrNullString()))
    }

    @DisplayName("회원가입")
    @WithMockDoriUser(loginId = GUEST_LOGIN_ID, role = Role.GUEST)
    @Test
    fun register() {
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val cookie = Cookie(REFRESH_TOKEN, guestRefreshToken.refreshToken)
        val body = RegisterRequest("Tester 2", MALE, "19970101", true, true, false)

        mockMvc.perform(
            post("$endPoint/register")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(cookie().value(REFRESH_TOKEN, notNullValue()))

        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("회원 탈퇴")
    @Test
    fun deactivate() {
        //given
        val user = User("010-1234-1234", "Tester", "010-1234-1234", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val point1 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))
        val point2 = pointRepository.save(Point(user.id, PointSaveType.REWARD, PointEventType.SAVE_REGISTER_REWARD, 10))
        val point3 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = DeactivateRequest(BankAccountDto("국민", "94320200120364", "김동준"))

        //when, then
        mockMvc.perform(
            delete(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(cookie().value(REFRESH_TOKEN, emptyOrNullString()))

        val findUser = userRepository.findByIdOrNull(user.id)

        val findPoint1 = pointRepository.findByIdOrNull(point1.id)
        val findPoint2 = pointRepository.findByIdOrNull(point2.id)
        val findPoint3 = pointRepository.findByIdOrNull(point3.id)

        val findInquiry = inquiryRepository.findAll().firstOrNull()

        assertThat(findUser?.active).isFalse()

        assertThat(findPoint1?.remainAmounts).isEqualTo(0)
        assertThat(findPoint2?.remainAmounts).isEqualTo(10)
        assertThat(findPoint3?.remainAmounts).isEqualTo(0)

        assertThat(findInquiry?.user?.id).isEqualTo(user.id)
        assertThat(findInquiry?.status).isEqualTo(InquiryStatus.PROCEEDING)
        assertThat(findInquiry?.content).isEqualTo("버블 20개 환불")
        assertThat(findInquiry?.bankAccount?.bank).isEqualTo("국민")
        assertThat(findInquiry?.bankAccount?.accountNumber).isEqualTo("94320200120364")
        assertThat(findInquiry?.bankAccount?.accountHolder).isEqualTo("김동준")
    }

    @DisplayName("회원 탈퇴 TC2")
    @Test
    fun deactivateTc2() {
        //given
        val user = User("010-1234-1234", "Tester", "010-1234-1234", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val point = pointRepository.save(Point(user.id, PointSaveType.REWARD, PointEventType.SAVE_REGISTER_REWARD, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = DeactivateRequest(BankAccountDto("국민", "94320200120364", "김동준"))

        //when, then
        mockMvc.perform(
            delete(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(cookie().value(REFRESH_TOKEN, emptyOrNullString()))

        val findUser = userRepository.findByIdOrNull(user.id)

        val findPoint = pointRepository.findByIdOrNull(point.id)

        val findInquiry = inquiryRepository.findAll().firstOrNull()

        assertThat(findUser?.active).isFalse()

        assertThat(findPoint?.remainAmounts).isEqualTo(10)

        assertThat(findInquiry?.user?.id).isEqualTo(user.id)
        assertThat(findInquiry?.status).isEqualTo(InquiryStatus.PROCEEDING)
        assertThat(findInquiry?.content).isEqualTo("버블 0개 환불")
        assertThat(findInquiry?.bankAccount?.bank).isEqualTo("국민")
        assertThat(findInquiry?.bankAccount?.accountNumber).isEqualTo("94320200120364")
        assertThat(findInquiry?.bankAccount?.accountHolder).isEqualTo("김동준")
    }

    @DisplayName("회원 탈퇴 TC3")
    @Test
    fun deactivateTc3() {
        //given
        val user = User("010-1234-1234", "Tester", "010-1234-1234", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val point = pointRepository.save(Point(user.id, PointSaveType.REWARD, PointEventType.SAVE_REGISTER_REWARD, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = DeactivateRequest(null)

        //when, then
        mockMvc.perform(
            delete(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(cookie().value(REFRESH_TOKEN, emptyOrNullString()))

        val findUser = userRepository.findByIdOrNull(user.id)

        val findPoint = pointRepository.findByIdOrNull(point.id)

        val findInquiries = inquiryRepository.findAll()

        assertThat(findUser?.active).isFalse()

        assertThat(findPoint?.remainAmounts).isEqualTo(10)

        assertThat(findInquiries).isEmpty()
    }

    @DisplayName("로그인 ID 변경 요청")
    @Test
    fun sendLoginIdChangeSms() {
        //given
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        doNothing().`when`(mockSmsService).sendLoginAuthSms(any<String>(), any<String>())
        val body = SendSmsRequest("010-1234-1234")

        //when
        mockMvc.perform(
            post("$endPoint/change-login-id/send-sms")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        //then
        verify(mockSmsService, times(1)).sendLoginAuthSms(any<String>(), any<String>())

        val findLoginChangeRequest = loginIdChangeRequestRepository.findByIdOrNull(user.id.toString())
        assertThat(findLoginChangeRequest?.toLoginId).isEqualTo("010-1234-1234")
    }

    @DisplayName("로그인 ID 변경 요청 예외")
    @Test
    fun sendLoginIdChangeSmsException() {
        //given
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val anotherUser = userRepository.save(User("010-1111-1111", "Another Tester", "010-1111-1111", Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        doNothing().`when`(mockSmsService).sendLoginAuthSms(any<String>(), any<String>())
        val body = SendSmsRequest("010-1111-1111")

        //when, then
        mockMvc.perform(
            post("$endPoint/change-login-id/send-sms")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("code", `is`(ErrorCode.USER_ALREADY_REGISTERED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.USER_ALREADY_REGISTERED.message)))
    }

    @DisplayName("로그인 ID 변경")
    @Test
    fun changeLoginId() {
        //given
        val authCode = "123456"
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        loginIdChangeRequestRepository.save(LoginIdChangeRequest(user.id.toString(), 300, "010-1111-1111", authCode))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val body = ChangeLoginIdRequest(authCode)

        //when, then
        mockMvc.perform(
            post("$endPoint/change-login-id")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
    }

    @DisplayName("로그인 ID 변경 예외")
    @Test
    fun changeLoginIdException() {
        //given
        val authCode = "123456"
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        loginIdChangeRequestRepository.save(LoginIdChangeRequest(user.id.toString(), 300, "010-1111-1111", authCode))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val body = ChangeLoginIdRequest("000000")

        //when, then
        mockMvc.perform(
            post("$endPoint/change-login-id")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("code", `is`(ErrorCode.LOGIN_ID_NOT_ALLOWED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.LOGIN_ID_NOT_ALLOWED.message)))
    }
}