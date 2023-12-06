package com.liah.doribottle.domain.task

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.task.dto.TaskDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.*

@Entity
@Table(
    name = "task",
    indexes = [
        Index(name = "INDEX_TASK_EXECUTE_DATE", columnList = "executeDate")
    ]
)
class Task(
    @Column(nullable = false)
    val executeDate: Instant,
    @Column(nullable = false)
    val type: TaskType,
    @Column(nullable = false)
    val targetId: UUID
) : PrimaryKeyEntity() {
    fun toDto() = TaskDto(id, executeDate, type, targetId)
}