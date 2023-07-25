package com.liah.doribottle.service.group

import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType.UNIVERSITY
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class GroupServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var groupService: GroupService
    @Autowired
    private lateinit var groupRepository: GroupRepository

    @DisplayName("기관 등록")
    @Test
    fun register() {
        //given, when
        val id = groupService.register("서울대학교", UNIVERSITY)
        clear()

        //then
        val findGroup = groupRepository.findByIdOrNull(id)
        assertThat(findGroup?.name).isEqualTo("서울대학교")
        assertThat(findGroup?.type).isEqualTo(UNIVERSITY)
    }

    @DisplayName("기관 조회")
    @Test
    fun get() {
        //given
        val group = groupRepository.save(Group("서울대학교", UNIVERSITY))
        clear()

        //when
        val groupDto = groupService.get(group.id)

        //then
        assertThat(groupDto.name).isEqualTo("서울대학교")
        assertThat(groupDto.type).isEqualTo(UNIVERSITY)
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
    }

    fun insertGroups() {
        groupRepository.save(Group("대학1", UNIVERSITY))
        groupRepository.save(Group("대학2", UNIVERSITY))
        groupRepository.save(Group("대학3", UNIVERSITY))
        groupRepository.save(Group("대학4", UNIVERSITY))
        groupRepository.save(Group("대학5", UNIVERSITY))
        groupRepository.save(Group("대학6", UNIVERSITY))
    }
}