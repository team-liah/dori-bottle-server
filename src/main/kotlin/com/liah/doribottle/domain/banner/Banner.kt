package com.liah.doribottle.domain.banner

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.banner.dto.BannerDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "banner")
class Banner(
    title: String,

    content: String,

    priority: Int,

    visible: Boolean,

    backgroundColor: String?,

    imageUrl: String?,

    targetUrl: String?
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    var title: String = title
        protected set

    @Column(nullable = false)
    var content: String = content
        protected set

    @Column(nullable = false)
    var priority: Int = priority
        protected set

    @Column(nullable = false)
    var visible: Boolean = visible
        protected set

    @Column
    var backgroundColor: String? = backgroundColor
        protected set

    @Column
    var imageUrl: String? = imageUrl
        protected set

    @Column
    var targetUrl: String? = targetUrl
        protected set

    fun update(
        title: String,
        content: String,
        priority: Int,
        visible: Boolean,
        backgroundColor: String?,
        imageUrl: String?,
        targetUrl: String?
    ) {
        this.title = title
        this.content = content
        this.priority = priority
        this.visible = visible
        this.backgroundColor = backgroundColor
        this.imageUrl = imageUrl
        this.targetUrl = targetUrl
    }

    fun toDto() = BannerDto(id, title, content, priority, visible, backgroundColor, imageUrl, targetUrl, createdDate, lastModifiedDate)
}