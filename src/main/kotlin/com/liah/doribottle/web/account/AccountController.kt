package com.liah.doribottle.web.account

import com.liah.doribottle.extension.currentUserLoginId
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.web.account.vm.*
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val smsService: SmsService
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
        @Valid @RequestBody request: AuthRequest
    ): AuthResponse {
        return accountService.auth(request.loginId!!, request.loginPassword!!)
            .toResponse()
    }

    @PostMapping("/register")
    fun register(
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

        return accountService.refreshAuth(currentUserLoginId()!!, refreshToken)
            .toResponse()
    }
}