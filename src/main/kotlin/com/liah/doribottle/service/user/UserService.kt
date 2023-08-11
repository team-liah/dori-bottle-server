package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.constant.SAVE_INVITE_REWARD_AMOUNTS_MAP
import com.liah.doribottle.constant.SAVE_REGISTER_INVITER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.repository.user.UserQueryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.user.dto.UserDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userQueryRepository: UserQueryRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional(readOnly = true)
    fun get(id: UUID) = userQueryRepository.get(id).toDetailDto()

    @Transactional(readOnly = true)
    fun getAll(
        name: String? = null,
        phoneNumber: String? = null,
        birthDate: String? = null,
        gender: Gender? = null,
        active: Boolean? = null,
        blocked: Boolean? = null,
        groupId: UUID? = null,
        pageable: Pageable
    ): Page<UserDto> {
        return userQueryRepository.getAll(
            name = name,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            gender = gender,
            active = active,
            blocked = blocked,
            groupId = groupId,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun update(
        id: UUID,
        name: String,
        birthDate: String,
        gender: Gender?
    ) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.update(name, birthDate, gender)
    }

    fun registerInvitationCode(
        inviteeId: UUID,
        invitationCode: String
    ) {
        val invitee = userRepository.findByIdOrNull(inviteeId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val inviter = userRepository.findByInvitationCode(invitationCode)
            ?: throw NotFoundException(ErrorCode.INVITER_NOT_FOUND)

        invitee.setInviter(inviter)

        // invitee reward
        applicationEventPublisher.publishEvent(
            PointSaveEvent(
                invitee.id,
                PointSaveType.REWARD,
                PointEventType.SAVE_REGISTER_INVITER_REWARD,
                SAVE_REGISTER_INVITER_REWARD_AMOUNTS
            )
        )

        if (invitee.use) {
            // inviter reward
            rewardInviter(inviter)
        }
    }

    fun rewardInviterByInvitee(
        inviteeId: UUID
    ) {
        val invitee = userRepository.findByIdOrNull(inviteeId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val inviter = invitee.inviterId?.let { userRepository.findByIdOrNull(it) }

        inviter?.let { rewardInviter(it) }
    }

    private fun rewardInviter(
        inviter: User
    ) {
        inviter.increaseInvitationCount()
        val inviteRewardAmounts = SAVE_INVITE_REWARD_AMOUNTS_MAP[inviter.invitationCount]
        if (inviteRewardAmounts != null) {
            applicationEventPublisher.publishEvent(
                PointSaveEvent(
                    inviter.id,
                    PointSaveType.REWARD,
                    PointEventType.SAVE_INVITE_REWARD,
                    inviteRewardAmounts
                )
            )
        }
    }
}