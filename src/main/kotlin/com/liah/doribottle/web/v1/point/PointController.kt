package com.liah.doribottle.web.v1.point

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.web.v1.point.vm.PointHistorySearchResponse
import com.liah.doribottle.web.v1.point.vm.PointHistorySearchType
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/point")
class PointController(
    private val pointService: PointService
) {
    @Operation(summary = "유저 잔여 포인트 조회")
    @GetMapping("/remain-point")
    fun getRemainPoint() = pointService.getSum(currentUserId()!!).toRemainPoint()

    @Operation(summary = "유저 포인트 이력 조회")
    @GetMapping("/history")
    fun getAllHistories(
        @RequestParam(value = "historyType", required = false) historyType: PointHistorySearchType?,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PointHistorySearchResponse> {
        val eventTypes = historyType?.let {
            when(it) {
                PointHistorySearchType.SAVE -> setOf(
                    PointEventType.SAVE_REGISTER_REWARD,
                    PointEventType.SAVE_REGISTER_INVITER_REWARD,
                    PointEventType.SAVE_INVITE_REWARD,
                    PointEventType.SAVE_PAY
                )
                PointHistorySearchType.USE -> setOf(
                    PointEventType.CANCEL_SAVE,
                    PointEventType.USE_CUP,
                    PointEventType.DISAPPEAR
                )
            }
        }

        val result = pointService.getAllHistories(
            userId = currentUserId()!!,
            eventTypes = eventTypes,
            pageable = pageable
        ).map { it.toResponse() }

        return CustomPage.of(result)
    }
}