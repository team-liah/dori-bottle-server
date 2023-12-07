package com.liah.doribottle.service.task.dto

import com.liah.doribottle.domain.task.TaskType
import java.time.Instant
import java.util.*

data class TaskDto(
    val id: UUID,
    val executeDate: Instant,
    val type: TaskType,
    val targetId: UUID
)