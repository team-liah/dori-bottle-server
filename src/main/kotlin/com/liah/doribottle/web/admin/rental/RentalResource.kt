package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.admin.rental.vm.RentalCupMapRequest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/rental")
class RentalResource(
    private val rentalService: RentalService
) {
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