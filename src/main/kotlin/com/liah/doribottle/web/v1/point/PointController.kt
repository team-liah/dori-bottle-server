package com.liah.doribottle.web.v1.point

import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.point.PointService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/point")
class PointController(
    private val pointService: PointService
) {
    //TODO: Caching
    @GetMapping("/remain-point")
    fun getRemainPoint() = pointService.getSum(currentUserId()!!).toRemainPoint()
}