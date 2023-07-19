package com.liah.doribottle.web.rental

import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.rental.vm.RentalRequest
import com.liah.doribottle.web.rental.vm.ReturnRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rental")
class RentalController(
    private val rentalService: RentalService
) {
    @PostMapping
    fun rental(
        @Valid @RequestBody request: RentalRequest
    ): UUID {
        return rentalService
            .rental(currentUserId()!!, request.cupRfid!!, request.machineId!!, request.withIce!!)
    }

    @PostMapping("/return")
    fun `return`(
        @Valid @RequestBody request: ReturnRequest
    ) {
        rentalService
            .`return`(request.machineId!!, request.cupRfid!!)
    }
}