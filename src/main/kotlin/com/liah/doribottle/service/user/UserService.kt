package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.constant.DoriConstant
import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.PenaltyType
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.event.Events
import com.liah.doribottle.messaging.AwsSqsSender
import com.liah.doribottle.messaging.vm.PointSaveMessage
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserQueryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.user.dto.UserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userQueryRepository: UserQueryRepository,
    private val groupRepository: GroupRepository,
    private val awsSqsSender: AwsSqsSender,
) {
    @Transactional(readOnly = true)
    fun get(id: UUID) = userQueryRepository.get(id).toDetailDto()

    @Transactional(readOnly = true)
    fun getAllByIds(ids: List<UUID>): List<UserDto> {
        return userRepository.findAllById(ids).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAll(
        name: String? = null,
        phoneNumber: String? = null,
        birthDate: String? = null,
        gender: Gender? = null,
        active: Boolean? = null,
        blocked: Boolean? = null,
        groupId: UUID? = null,
        pageable: Pageable,
    ): Page<UserDto> {
        return userQueryRepository.getAll(
            name = name,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            gender = gender,
            active = active,
            blocked = blocked,
            groupId = groupId,
            pageable = pageable,
        ).map { it.toDto() }
    }

    fun update(
        id: UUID,
        name: String,
        birthDate: String? = null,
        gender: Gender? = null,
        description: String? = null,
        groupId: UUID? = null,
    ) {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val group =
            groupId?.let {
                groupRepository.findByIdOrNull(groupId)
                    ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)
            }

        user.update(name, birthDate, gender, description)
        user.updateGroup(group)
    }

    fun registerInvitationCode(
        inviteeId: UUID,
        invitationCode: String,
    ) {
        val invitee =
            userRepository.findByIdOrNull(inviteeId)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val inviter =
            userRepository.findByInvitationCode(invitationCode)
                ?: throw NotFoundException(ErrorCode.INVITER_NOT_FOUND)

        // invitee reward
        rewardInvitee(invitee, inviter)

        // inviter reward
        rewardInviter(inviter)
    }

    private fun rewardInvitee(
        invitee: User,
        inviter: User,
    ) {
        invitee.setInviter(inviter)
        awsSqsSender.send(
            PointSaveMessage(
                invitee.id,
                PointSaveType.REWARD,
                PointEventType.SAVE_REGISTER_INVITER_REWARD,
                DoriConstant.SAVE_REGISTER_INVITER_REWARD_AMOUNTS,
            ),
        )
    }

    private fun rewardInviter(inviter: User) {
        inviter.increaseInvitationCount()
        val inviteRewardAmounts = DoriConstant.SAVE_INVITE_REWARD_AMOUNTS_MAP[inviter.invitationCount]
        if (inviteRewardAmounts != null) {
            awsSqsSender.send(
                PointSaveMessage(
                    inviter.id,
                    PointSaveType.REWARD,
                    PointEventType.SAVE_INVITE_REWARD,
                    inviteRewardAmounts,
                ),
            )
        }
    }

    fun imposePenalty(
        id: UUID,
        penaltyType: PenaltyType,
        penaltyCause: String? = null,
    ) {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.imposePenalty(penaltyType, penaltyCause)

        Events.notify(
            NotificationIndividual(
                userId = user.id,
                type = NotificationType.PENALTY,
                targetId = null,
                penaltyType.title,
            ),
        )
    }

    fun removePenalty(
        id: UUID,
        penaltyId: UUID,
    ) {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.removePenalty(penaltyId)

        Events.notify(
            NotificationIndividual(
                userId = user.id,
                type = NotificationType.PENALTY_CANCEL,
                targetId = null,
            ),
        )
    }

    fun block(
        id: UUID,
        blockedCauseType: BlockedCauseType,
        blockedCauseDescription: String? = null,
    ) {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.block(blockedCauseType, blockedCauseDescription)
    }

    fun unblock(
        id: UUID,
        blockedCauseIds: Set<UUID>,
    ) {
        val user =
            userRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        blockedCauseIds.forEach { blockedCauseId ->
            user.unblock(blockedCauseId)
        }
    }
}
