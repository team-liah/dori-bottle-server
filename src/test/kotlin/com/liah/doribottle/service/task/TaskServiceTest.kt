package com.liah.doribottle.service.task

import com.liah.doribottle.domain.task.Task
import com.liah.doribottle.domain.task.TaskType
import com.liah.doribottle.repository.task.TaskRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class TaskServiceTest : BaseServiceTest() {
    @Autowired private lateinit var taskService: TaskService
    @Autowired private lateinit var taskRepository: TaskRepository

    @DisplayName("작업 등록")
    @Test
    fun register() {
        //given
        val executeDate = Instant.now()
        val type = TaskType.RENTAL_REMIND
        val targetId = UUID.randomUUID()

        //when
        val id = taskService.register(executeDate, type, targetId)
        clear()

        //then
        val findTask = taskRepository.findByIdOrNull(id)
        assertThat(findTask?.executeDate).isEqualTo(executeDate)
        assertThat(findTask?.type).isEqualTo(type)
        assertThat(findTask?.targetId).isEqualTo(targetId)
    }

    @DisplayName("실행 작업 조회")
    @Test
    fun getAllForExecute() {
        //given
        val executeDate1 = Instant.now().minus(5, ChronoUnit.HOURS)
        val executeDate2 = Instant.now().plus(5, ChronoUnit.HOURS)
        val executeDate3 = Instant.now().minus(5, ChronoUnit.HOURS)
        val executeDate4 = Instant.now().plus(5, ChronoUnit.HOURS)
        val executeDate5 = Instant.now().minus(5, ChronoUnit.HOURS)
        val type = TaskType.RENTAL_REMIND
        val targetId = UUID.randomUUID()

        taskRepository.saveAll(
            listOf(
                Task(executeDate1, type, targetId),
                Task(executeDate2, type, targetId),
                Task(executeDate3, type, targetId),
                Task(executeDate4, type, targetId),
                Task(executeDate5, type, targetId)
            )
        )
        clear()

        //when
        val result = taskService.getAllForExecute()

        //then
        assertThat(result.size).isEqualTo(3)
        assertThat(result)
            .extracting("executeDate")
            .containsExactly(executeDate1, executeDate3, executeDate5)
    }

    @DisplayName("작업 삭제")
    @Test
    fun delete() {
        //given
        val executeDate = Instant.now()
        val type = TaskType.RENTAL_REMIND
        val targetId = UUID.randomUUID()
        val task = taskRepository.save(Task(executeDate, type, targetId))
        clear()

        //when
        taskService.delete(task.id)
        clear()

        val findTask = taskRepository.findByIdOrNull(task.id)
        assertThat(findTask).isNull()
    }
}