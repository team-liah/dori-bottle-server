package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.service.rental.dto.RentalDto
import com.liah.doribottle.service.rental.dto.RentalStatisticDto
import com.liah.doribottle.web.admin.rental.vm.RentalSearchRequest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.time.Month
import java.time.Year
import java.util.*

@RestController
@RequestMapping("/admin/api/rental")
class RentalResource(
    private val rentalService: RentalService,
) {
    @Operation(summary = "유저 대여 내역 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: RentalSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): CustomPage<RentalDto> {
        val result =
            rentalService.getAll(
                no = request.no,
                userId = request.userId,
                cupId = request.cupId,
                fromMachineId = request.fromMachineId,
                toMachineId = request.toMachineId,
                status = request.status,
                expired = request.expired,
                pageable = pageable,
            )

        return CustomPage.of(result)
    }

    @Operation(summary = "유저 대여 내역 단건 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): RentalDto {
        return rentalService.get(id)
    }

    @Operation(summary = "반납 처리")
    @PostMapping("/return")
    fun `return`(
        @Valid @RequestBody request: ReturnRequest,
    ) {
        rentalService.`return`(
            toMachineNo = request.machineNo!!,
            cupRfid = request.cupRfid!!,
        )
    }

    @Operation(summary = "대여 취소")
    @PostMapping("/{id}/cancel")
    fun cancel(
        @PathVariable id: UUID,
    ) {
        rentalService.cancel(id)
    }

    // TODO: Test
    @GetMapping("/statistic")
    fun getStatistic(
        @RequestParam(value = "year") year: Year,
        @RequestParam(value = "month", required = false) month: Month?,
    ): List<RentalStatisticDto> {
        return rentalService.getStatistic(
            year = year,
            month = month,
        )
    }
}
