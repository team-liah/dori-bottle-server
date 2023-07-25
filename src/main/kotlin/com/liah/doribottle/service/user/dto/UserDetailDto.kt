package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.web.v1.me.vm.MeResponse
import java.util.*

data class UserDetailDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role,
    val penalties: List<PenaltyDto>
) {
    fun toMeResponse() = MeResponse(id, loginId, name, phoneNumber, invitationCode, birthDate, gender, role, penalties)
}