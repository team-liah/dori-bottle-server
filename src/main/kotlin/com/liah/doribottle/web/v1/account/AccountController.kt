package com.liah.doribottle.web.v1.account

import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.event.dummy.DummyInitEvent
import com.liah.doribottle.extension.*
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.inquiry.InquiryService
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.web.v1.account.vm.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val smsService: SmsService,
    private val pointService: PointService,
    private val inquiryService: InquiryService,
    @Value("\${app.auth.jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.auth.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long,

    // TODO: Remove
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Operation(summary = "인증 SMS 발송 요청")
    @PostMapping("/auth/send-sms")
    fun sendAuthSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = generateRandomNumberString()
        accountService.saveOrUpdatePassword(request.loginId!!, authCode)

        smsService.sendLoginAuthSms(request.loginId, authCode)
    }

    @Operation(summary = "인증")
    @PostMapping("/auth")
    fun auth(
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val result = accountService.auth(
            loginId = request.loginId!!,
            loginPassword = request.loginPassword!!
        )

        val accessTokenCookie = createCookie(
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toResponse())
    }

    @Operation(summary = "인증 Refresh")
    @PostMapping("/refresh-auth")
    fun refreshAuth(
        @CookieValue("refresh_token") refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        val result = accountService.refreshAuth(refreshToken)

        val accessTokenCookie = createCookie(
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toResponse())
    }

    @Operation(summary = "자판기 전용 인증 토큰 발급")
    @GetMapping("/pre-auth")
    fun preAuth() = PreAuthResponse(accountService.preAuth(currentUser()!!))

    @Operation(summary = "회원 등록")
    @PostMapping("/register")
    fun register(
        @CookieValue("refresh_token") refreshToken: String?,
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        accountService.register(
            loginId = currentUserLoginId()!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender,
            agreedTermsOfService = request.agreedTermsOfService!!,
            agreedTermsOfPrivacy = request.agreedTermsOfPrivacy!!,
            agreedTermsOfMarketing = request.agreedTermsOfMarketing!!
        )

        val result = try {
            accountService.refreshAuth(refreshToken)
        } catch (e: UnauthorizedException) {
            null
        }

        return if (result == null) {
            val expiredAccessTokenCookie = expireCookie(ACCESS_TOKEN)
            val expiredRefreshTokenCookie = expireCookie(REFRESH_TOKEN)
            ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
                .build()
        } else {
            val accessTokenCookie = createCookie(
                name = ACCESS_TOKEN,
                value = result.accessToken,
                expiredMs = jwtExpiredMs
            )
            val refreshTokenCookie = createCookie(
                name = REFRESH_TOKEN,
                value = result.refreshToken,
                expiredMs = refreshTokenExpiredMs
            )
            ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(result.toResponse())
        }
    }

    @Operation(summary = "회원 탈퇴")
    @PostMapping("/inactivate")
    fun inactivate(
        @Valid @RequestBody request: DeactivateRequest
    ): ResponseEntity<Void> {
        val currentUserId = currentUserId()!!
        accountService.inactivate(currentUserId)

        if (request.bankAccount != null) {
            val remainPayPoints = pointService.getAllRemainByUserId(currentUserId)
                .filter { it.saveType == PointSaveType.PAY }
            var remainPayAmounts = 0L
            if (remainPayPoints.isNotEmpty()) {
                remainPayPoints.forEach { point ->
                    remainPayAmounts += point.remainAmounts
                    pointService.expire(
                        id = point.id,
                        userId = currentUserId
                    )
                }
            }

            inquiryService.register(
                userId = currentUserId,
                type = InquiryType.REFUND,
                bankAccount = request.bankAccount,
                content = "버블 ${remainPayAmounts}개 환불"
            )
        }

        val expiredAccessTokenCookie = expireCookie(ACCESS_TOKEN)
        val expiredRefreshTokenCookie = expireCookie(REFRESH_TOKEN)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
            .build()
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> {
        val expiredAccessTokenCookie = expireCookie(ACCESS_TOKEN)
        val expiredRefreshTokenCookie = expireCookie(REFRESH_TOKEN)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
            .build()
    }

    @Operation(summary = "로그인 ID 변경 SMS 발송 요청")
    @PostMapping("/change-login-id/send-sms")
    fun sendLoginIdChangeSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = generateRandomNumberString()
        accountService.createLoginIdChange(
            userId = currentUserId()!!,
            toLoginId = request.loginId!!,
            authCode = authCode
        )

        smsService.sendLoginAuthSms(request.loginId, authCode)
    }

    @Operation(summary = "로그인ID 변경")
    @PutMapping("/change-login-id")
    fun changeLoginId(
        @Valid @RequestBody request: LoginIdChangeRequest
    ): ResponseEntity<Void> {
        accountService.changeLoginId(
            userId = currentUserId()!!,
            authCode = request.authCode!!
        )

        val expiredAccessTokenCookie = expireCookie(ACCESS_TOKEN)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .build()
    }

    //TODO: Remove
    @PostMapping("/dummy-auth")
    fun dummyAuth(): ResponseEntity<AuthResponse> {
        val result = accountService.dummyAuth()

        val accessTokenCookie = createCookie(
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toResponse())
    }

    //TODO: Remove
    @PostMapping("/dummy-data")
    fun dummyData() {
        applicationEventPublisher.publishEvent(
            DummyInitEvent(true)
        )
    }
}