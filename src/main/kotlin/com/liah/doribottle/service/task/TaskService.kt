package com.liah.doribottle.service.task

import com.liah.doribottle.domain.task.Task
import com.liah.doribottle.domain.task.TaskType
import com.liah.doribottle.repository.task.TaskRepository
import com.liah.doribottle.service.task.dto.TaskDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class TaskService(
    private val taskRepository: TaskRepository
) {
    fun register(
        executeDate: Instant,
        type: TaskType,
        targetId: UUID
    ): UUID {
        val task = taskRepository.save(Task(executeDate, type, targetId))

        return task.id
    }

    @Transactional(readOnly = true)
    fun getAllForExecute(): List<TaskDto> {
        return taskRepository.findAllByExecuteDateBefore(Instant.now())
            .map { it.toDto() }
    }

    fun delete(
        id: UUID
    ) {
        taskRepository.deleteById(id)
    }
}