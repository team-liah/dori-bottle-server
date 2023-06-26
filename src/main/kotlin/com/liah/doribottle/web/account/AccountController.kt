package com.liah.doribottle.web.account

import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.web.account.vm.*
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    @PostMapping("/auth/send-sms")
    fun sendSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = Random().nextInt(100000, 999999).toString()

        accountService.authRequest(request.loginId!!, authCode)

        // Send SMS
        log.info("===================")
        log.info("PASSWORD: $authCode")
        log.info("===================")
    }

    @PostMapping("/auth")
    fun auth(
        @Valid @RequestBody request: AuthRequest
    ): AuthResponse {
        val accessToken = accountService.auth(request.loginId!!, request.loginPassword!!)
        return AuthResponse(accessToken)
    }

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ) {
        accountService.register(
            id = currentUserId()!!,
            phoneNumber = request.phoneNumber!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender!!
        )

        // TODO: Refresh token
    }
}