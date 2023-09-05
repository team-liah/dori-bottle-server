package com.liah.doribottle.web.v1.notification

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.web.v1.notification.vm.NotificationSearchResponse
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/notification")
class NotificationController(
    private val notificationService: NotificationService
) {
    @Operation(summary = "알림 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = DESC) pageable: Pageable
    ): CustomPage<NotificationSearchResponse> {
        val result = notificationService.getAll(
            userId = currentUserId()!!,
            pageable = pageable
        ).map { it.toSearchResponse() }

        notificationService.clearAlert(currentUserId()!!)

        return CustomPage.of(result)
    }

    @Operation(summary = "알림 읽음 처리")
    @PutMapping("/{id}/read")
    fun read(
        @PathVariable id: UUID
    ) {
        notificationService.read(id)
    }
}