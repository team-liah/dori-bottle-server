package com.liah.doribottle.web.admin.me

import com.liah.doribottle.extension.currentUser
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api/me")
class AdminMeController {
    @Operation(summary = "로그인 관리자 정보 조회")
    @GetMapping
    fun get() = currentUser()
}