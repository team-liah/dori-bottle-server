package com.liah.doribottle.domain.board

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.board.dto.PostDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(
    name = "post",
    indexes = [Index(name = "IDX_POST_BOARD_TYPE", columnList = "board_type")]
)
class Post(
    board: Board,
    author: Author,
    title: String,
    content: String,
    notify: Boolean
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(referencedColumnName = "board_type", name = "board_type", nullable = false)
    var board: Board = board
        protected set

    @Embedded
    val author: Author = author

    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(nullable = false)
    var content: String = content
        protected set

    @Column(nullable = false)
    var notify: Boolean = notify
        protected set

    fun update(
        title: String,
        content: String,
        notify: Boolean
    ) {
        this.title = title
        this.content = content
        this.notify = notify
    }

    fun toDto() = PostDto(author.toDto(), board.type, title, content, notify)
}