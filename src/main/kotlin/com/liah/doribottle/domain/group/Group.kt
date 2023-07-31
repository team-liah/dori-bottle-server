package com.liah.doribottle.domain.group

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.group.dto.GroupDto
import jakarta.persistence.*

@Entity
@Table(name = "`group`")
class Group(
    name: String,
    type: GroupType
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: GroupType = type
        protected set

    fun update(
        name: String,
        type: GroupType
    ) {
        this.name = name
        this.type = type
    }

    fun toDto() = GroupDto(id, name, type)
}