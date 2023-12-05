package com.liah.doribottle.web.v1.me

import com.liah.doribottle.extension.currentUser
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.v1.me.vm.InvitationCodeRegisterRequest
import com.liah.doribottle.web.v1.me.vm.MeResponse
import com.liah.doribottle.web.v1.me.vm.ProfileUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me")
class MeController(
    private val userService: UserService,
    private val notificationService: NotificationService
) {
    @Operation(summary = "로그인 유저 정보 조회")
    @GetMapping
    fun get(): MeResponse {
        val doriUser = currentUser()!!
        return MeResponse.of(doriUser, notificationService.getAlertCount(doriUser.id))
    }

    @Operation(summary = "로그인 유저 프로필 조회")
    @GetMapping("/profile")
    fun getProfile() = userService.get(currentUserId()!!).toProfileResponse()

    @Operation(summary = "로그인 유저 프로필 업데이트")
    @PutMapping("/profile")
    fun updateProfile(
        @Valid @RequestBody request: ProfileUpdateRequest
    ) {
        val currentUser = userService.get(currentUserId()!!)
        userService.update(
            id = currentUser.id,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender,
            description = currentUser.description,
            groupId = currentUser.group?.id
        )
    }

    @Operation(summary = "초대코드 등록")
    @PostMapping("/invitation-code")
    fun registerInvitationCode(
        @Valid @RequestBody request: InvitationCodeRegisterRequest
    ) {
        userService.registerInvitationCode(currentUserId()!!, request.invitationCode!!)
    }
}