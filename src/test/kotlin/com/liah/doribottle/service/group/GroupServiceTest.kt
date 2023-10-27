package com.liah.doribottle.service.group

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType.COMPANY
import com.liah.doribottle.domain.group.GroupType.UNIVERSITY
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class GroupServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var groupService: GroupService
    @Autowired
    private lateinit var groupRepository: GroupRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("기관 등록")
    @Test
    fun register() {
        //given, when
        val id = groupService.register("서울대학교", UNIVERSITY, 30)
        clear()

        //then
        val findGroup = groupRepository.findByIdOrNull(id)
        assertThat(findGroup?.name).isEqualTo("서울대학교")
        assertThat(findGroup?.type).isEqualTo(UNIVERSITY)
        assertThat(findGroup?.discountRate).isEqualTo(30)
    }

    @DisplayName("기관 조회")
    @Test
    fun get() {
        //given
        val group = groupRepository.save(Group("서울대학교", UNIVERSITY, 30))
        clear()

        //when
        val groupDto = groupService.get(group.id)

        //then
        assertThat(groupDto.name).isEqualTo("서울대학교")
        assertThat(groupDto.type).isEqualTo(UNIVERSITY)
        assertThat(groupDto.discountRate).isEqualTo(30)
    }

    @DisplayName("기관 목록 조회")
    @Test
    fun getAll() {
        //given
        insertGroups()
        clear()

        //when
        val result = groupService.getAll(
            name = "대학",
            type = UNIVERSITY,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("name")
            .containsExactly("대학1", "대학2", "대학3")
        assertThat(result)
            .extracting("type")
            .containsExactly(UNIVERSITY, UNIVERSITY, UNIVERSITY)
        assertThat(result)
            .extracting("discountRate")
            .containsExactly(10, 10, 10)
    }

    @DisplayName("기관 목록 조회")
    @Test
    fun findByUserId() {
        val user1 = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        val user2 = userRepository.save(User("010-0000-0000", "Tester 2", "010-0000-0000", Role.USER))
        val group = groupRepository.save(Group("서울대학교", UNIVERSITY, 30))
        user1.updateGroup(group)
        clear()

        val result1 = groupService.findByUserId(user1.id)
        val result2 = groupService.findByUserId(user2.id)

        assertThat(result1?.id).isEqualTo(group.id)
        assertThat(result2).isNull()
    }

    @DisplayName("기관 수정")
    @Test
    fun update() {
        //given
        val group = groupRepository.save(Group("서울대학교", UNIVERSITY, 30))
        clear()

        //when
        groupService.update(group.id, "리아", COMPANY, 10)
        clear()

        //then
        val findGroup = groupRepository.findByIdOrNull(group.id)
        assertThat(findGroup?.name).isEqualTo("리아")
        assertThat(findGroup?.type).isEqualTo(COMPANY)
        assertThat(findGroup?.discountRate).isEqualTo(10)
    }

    @DisplayName("기관 삭제")
    @Test
    fun delete() {
        //given
        val group = groupRepository.save(Group("서울대학교", UNIVERSITY, 10))
        val user1 = userRepository.save(User("010-0000-0001", "Tester 1", "010-0000-0001", Role.USER))
        val user2 = userRepository.save(User("010-0000-0002", "Tester 2", "010-0000-0002", Role.USER))
        user1.updateGroup(group)
        user2.updateGroup(group)
        clear()

        //when
        groupService.delete(group.id)
        clear()

        //then
        val findGroup = groupRepository.findByIdOrNull(group.id)
        val findUser1 = userRepository.findByIdOrNull(user1.id)
        val findUser2 = userRepository.findByIdOrNull(user2.id)
        assertThat(findGroup).isNull()
        assertThat(findUser1?.group).isNull()
        assertThat(findUser2?.group).isNull()
    }

    @DisplayName("유저 추가")
    @Test
    fun addUser() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        val group = groupRepository.save(Group("리아", COMPANY, 10))
        clear()

        groupService.addUser(group.id, user.id)
        clear()

        val findUser = userRepository.findByIdOrNull(user.id)
        assertThat(findUser?.group).isEqualTo(group)
    }

    @DisplayName("유저 제거")
    @Test
    fun removeUser() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        val group = groupRepository.save(Group("리아", COMPANY, 10))
        user.updateGroup(group)
        clear()

        groupService.removeUser(group.id, user.id)
        clear()

        val findUser = userRepository.findByIdOrNull(user.id)
        assertThat(findUser?.group).isNull()
    }

    @DisplayName("유저 제거 예외")
    @Test
    fun removeUserException() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        val group1 = groupRepository.save(Group("리아", COMPANY, 10))
        val group2 = groupRepository.save(Group("삼성", COMPANY, 10))
        user.updateGroup(group1)
        clear()

        val exception = assertThrows<BusinessException> {
            groupService.removeUser(group2.id, user.id)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.GROUP_NOT_MEMBER)
    }

    private fun insertGroups() {
        groupRepository.save(Group("대학1", UNIVERSITY, 10))
        groupRepository.save(Group("대학2", UNIVERSITY, 10))
        groupRepository.save(Group("대학3", UNIVERSITY, 10))
        groupRepository.save(Group("대학4", UNIVERSITY, 10))
        groupRepository.save(Group("대학5", UNIVERSITY, 10))
        groupRepository.save(Group("대학6", UNIVERSITY, 10))
    }
}