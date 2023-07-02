package com.liah.doribottle.web.account

import com.liah.doribottle.extension.currentUserLoginId
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.web.account.vm.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val smsService: SmsService,
    @Value("\${jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long
) {
    @PostMapping("/auth/send-sms")
    fun sendSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = Random().nextInt(100000, 999999).toString()
        accountService.updatePassword(request.loginId!!, authCode)

        smsService.sendLoginAuthSms(request.loginId, authCode)
    }

    @PostMapping("/auth")
    fun auth(
        httpRequest: HttpServletRequest,
        @Valid @RequestBody request: AuthRequest
    ): AuthResponse {
        val result = accountService.auth(request.loginId!!, request.loginPassword!!)

        // TODO: Set Cookie
//        val accessTokenCookie = createCookie(
//            url = httpRequest.requestURL.toString(),
//            name = "access_token",
//            value = result.accessToken,
//            expiredMs = jwtExpiredMs
//        )
//        val refreshTokenCookie = createCookie(
//            url = httpRequest.requestURL.toString(),
//            name = "refresh_token",
//            value = result.refreshToken,
//            expiredMs = refreshTokenExpiredMs
//        )

        return result.toResponse()
    }

    @PostMapping("/register")
    fun register(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?,
        @Valid @RequestBody request: RegisterRequest
    ): AuthResponse {
        accountService.register(
            loginId = currentUserLoginId()!!,
            phoneNumber = request.phoneNumber!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender!!
        )
        val result = accountService.refreshAuth(currentUserLoginId()!!, refreshToken, refreshTokenExpiredMs)

        // TODO: Set Cookie
//        val accessTokenCookie = createCookie(
//            url = httpRequest.requestURL.toString(),
//            name = "access_token",
//            value = result.accessToken,
//            expiredMs = jwtExpiredMs
//        )
//        val refreshTokenCookie = createCookie(
//            url = httpRequest.requestURL.toString(),
//            name = "refresh_token",
//            value = result.refreshToken,
//            expiredMs = refreshTokenExpiredMs
//        )

        return result.toResponse()
    }
}