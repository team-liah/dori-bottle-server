package com.liah.doribottle.service.banner

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.banner.Banner
import com.liah.doribottle.repository.banner.BannerQueryRepository
import com.liah.doribottle.repository.banner.BannerRepository
import com.liah.doribottle.service.banner.dto.BannerDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class BannerService(
    private val bannerRepository: BannerRepository,
    private val bannerQueryRepository: BannerQueryRepository
) {
    fun register(
        title: String,
        header: String? = null,
        content: String? = null,
        priority: Int,
        visible: Boolean,
        backgroundColor: String? = null,
        backgroundImageUrl: String? = null,
        imageUrl: String? = null,
        targetUrl: String? = null
    ): UUID {
        val banner = bannerRepository.save(Banner(title, header, content, priority, visible, backgroundColor, backgroundImageUrl, imageUrl, targetUrl))

        return banner.id
    }

    fun update(
        id: UUID,
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
        val banner = bannerRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.BANNER_NOT_FOUND)

        banner.update(title, header, content, priority, visible, backgroundColor, backgroundImageUrl, imageUrl, targetUrl)
    }

    @Transactional(readOnly = true)
    fun get(id: UUID): BannerDto {
        val banner =  bannerRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.BANNER_NOT_FOUND)

        return banner.toDto()
    }

    @Transactional(readOnly = true)
    fun getAll(
        title: String? = null,
        header: String? = null,
        content: String? = null,
        visible: Boolean? = null,
        pageable: Pageable
    ): Page<BannerDto> {
        return bannerQueryRepository.getAll(
            title = title,
            content = content,
            visible = visible,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAll(
        title: String? = null,
        header: String? = null,
        content: String? = null,
        visible: Boolean? = null
    ): List<BannerDto> {
        return bannerQueryRepository.getAll(
            title = title,
            content = content,
            visible = visible
        ).map { it.toDto() }
    }

    fun delete(id: UUID) {
        bannerRepository.deleteById(id)
    }
}