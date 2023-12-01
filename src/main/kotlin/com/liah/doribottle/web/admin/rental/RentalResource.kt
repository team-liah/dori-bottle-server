package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.service.rental.dto.RentalDto
import com.liah.doribottle.web.admin.rental.vm.RentalCupMapRequest
import com.liah.doribottle.web.admin.rental.vm.RentalSearchRequest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/rental")
class RentalResource(
    private val rentalService: RentalService
) {
    @Operation(summary = "유저 대여 내역 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: RentalSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<RentalDto> {
        val result = rentalService.getAll(
            no = request.no,
            userId = request.userId,
            cupId = request.cupId,
            fromMachineId = request.fromMachineId,
            toMachineId = request.toMachineId,
            status = request.status,
            expired = request.expired,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "유저 대여 내역 단건 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): RentalDto {
        return rentalService.get(id)
    }

    @Operation(summary = "대여 정보 - 컵 매핑")
    @PostMapping("/{id}/map")
    fun mapRentalCup(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RentalCupMapRequest
    ) {
        rentalService.updateRentalCup(
            id = id,
            cupRfid = request.cupRfid!!
        )
    }

    @Operation(summary = "반납 처리")
    @PostMapping("/return")
    fun `return`(
        @Valid @RequestBody request: ReturnRequest
    ) {
        rentalService.`return`(
            toMachineNo = request.machineNo!!,
            cupRfid = request.cupRfid!!
        )
    }
}