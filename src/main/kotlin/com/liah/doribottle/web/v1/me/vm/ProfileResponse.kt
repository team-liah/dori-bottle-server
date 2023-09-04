package com.liah.doribottle.web.v1.me.vm

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.group.dto.GroupDto
import com.liah.doribottle.service.user.dto.BlockedCauseDto
import java.time.Instant
import java.util.*

data class ProfileResponse(
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
    val group: GroupDto?,
    val penaltyCount: Int,
    val blocked: Boolean,
    val blockedCauseDto: List<BlockedCauseDto>
)