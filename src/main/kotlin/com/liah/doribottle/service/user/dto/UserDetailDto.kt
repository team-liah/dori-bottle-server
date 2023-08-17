package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.group.dto.GroupDto
import com.liah.doribottle.web.admin.user.vm.UserDetailResponse
import com.liah.doribottle.web.v1.me.vm.ProfileResponse
import java.time.Instant
import java.util.*

data class UserDetailDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val invitationCount: Int,
    val inviterId: UUID?,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role,
    val registeredDate: Instant?,
    val penalties: List<PenaltyDto>,
    val group: GroupDto?
) {
    fun toProfileResponse() = ProfileResponse(id, loginId, name, phoneNumber, invitationCode, invitationCount, inviterId, birthDate, gender, role, registeredDate, penalties.size, group)
    fun toResponse() = UserDetailResponse(id, loginId, name, phoneNumber, invitationCode, invitationCount, inviterId, birthDate, gender, role, registeredDate, penalties, group)
}