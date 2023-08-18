package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.admin.rental.vm.RentalCupUpdateRequest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/rental")
class RentalResource(
    private val rentalService: RentalService
) {
    @PutMapping("/{id}/cup")
    fun updateRentalCup(
        @PathVariable id: UUID,
        @Valid @RequestBody request: RentalCupUpdateRequest
    ) {
        rentalService.updateRentalCup(
            id = id,
            cupRfid = request.cupRfid!!
        )
    }

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