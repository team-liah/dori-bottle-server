package com.liah.doribottle.web.v1.point

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.web.v1.point.vm.PointHistorySearchResponse
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
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
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PointHistorySearchResponse> {
        val result = pointService.getAllHistories(
            userId = currentUserId()!!,
            pageable = pageable
        ).map { it.toResponse() }

        return CustomPage.of(result)
    }
}