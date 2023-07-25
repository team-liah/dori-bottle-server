package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api/rental")
class RentalResource(
    private val rentalService: RentalService
) {
    @PostMapping("/return")
    fun `return`(
        @Valid @RequestBody request: ReturnRequest
    ) {
        rentalService.`return`(
            toMachineId = request.machineId!!,
            cupRfid = request.cupRfid!!
        )
    }
}