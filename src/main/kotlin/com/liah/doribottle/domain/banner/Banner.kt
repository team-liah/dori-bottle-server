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

    header: String? = null,

    content: String? = null,

    priority: Int,

    visible: Boolean,

    backgroundColor: String? = null,

    backgroundImageUrl: String? = null,

    imageUrl: String? = null,

    targetUrl: String? = null
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    var title: String = title
        protected set

    @Column
    var header: String? = header
        protected set

    @Column
    var content: String? = content
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
    var backgroundImageUrl: String? = backgroundImageUrl
        protected set

    @Column
    var imageUrl: String? = imageUrl
        protected set

    @Column
    var targetUrl: String? = targetUrl
        protected set

    fun update(
        title: String,
        header: String? = null,
        content: String? = null,
        priority: Int,
        visible: Boolean,
        backgroundColor: String? = null,
        backgroundImageUrl: String? = null,
        imageUrl: String? = null,
        targetUrl: String? = null
    ) {
        this.title = title
        this.content = content
        this.priority = priority
        this.visible = visible
        this.backgroundColor = backgroundColor
        this.backgroundImageUrl = backgroundImageUrl
        this.imageUrl = imageUrl
        this.targetUrl = targetUrl
    }

    fun toDto() = BannerDto(id, title, header, content, priority, visible, backgroundColor, backgroundImageUrl, imageUrl, targetUrl, createdDate, lastModifiedDate)
}