package com.liah.doribottle.web.me.vm

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import java.util.*

data class ProfileResponse(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: Int?,
    val gender: Gender?,
    val role: Role
)