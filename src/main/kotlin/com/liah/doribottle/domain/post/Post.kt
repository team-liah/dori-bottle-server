package com.liah.doribottle.domain.post

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.service.post.dto.PostDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(
    name = "post",
    indexes = [Index(name = "IDX_POST_TYPE", columnList = "type")]
)
class Post(
    author: Admin,
    type: PostType,
    title: String,
    content: String
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    val author: Admin = author

    @Column(nullable = false)
    var type: PostType = type
        protected set

    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(nullable = false)
    var content: String = content
        protected set

    fun update(
        type: PostType,
        title: String,
        content: String
    ) {
        this.type = type
        this.title = title
        this.content = content
    }

    fun toDto() = PostDto(author.id, type, title, content)
}