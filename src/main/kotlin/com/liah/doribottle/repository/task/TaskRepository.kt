package com.liah.doribottle.repository.task

import com.liah.doribottle.domain.task.Task
import com.liah.doribottle.domain.task.TaskType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface TaskRepository : JpaRepository<Task, UUID> {
    fun findByTypeAndTargetId(type: TaskType, targetId: UUID): Task?
    fun findAllByExecuteDateBefore(executeDate: Instant): List<Task>
}