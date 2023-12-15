package com.liah.doribottle.domain.group

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.extension.generateRandomNumberString
import com.liah.doribottle.service.group.dto.GroupDto
import jakarta.persistence.*

@Entity
@Table(
    name = "`group`",
    indexes = [Index(name = "IDX_GROUP_CODE", columnList = "code")]
)
class Group(
    name: String,
    type: GroupType,
    discountRate: Int
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val code: String = "GROUP_${type.name}_CODE_${generateRandomNumberString()}"

    @Column(nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: GroupType = type
        protected set

    @Column(nullable = false)
    var discountRate: Int = discountRate
        protected set

    fun update(
        name: String,
        type: GroupType,
        discountRate: Int
    ) {
        this.name = name
        this.type = type
        this.discountRate = discountRate
    }

    fun toDto() = GroupDto(id, code, name, type, discountRate, createdDate, lastModifiedDate)
}