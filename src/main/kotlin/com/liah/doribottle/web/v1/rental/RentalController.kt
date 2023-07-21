package com.liah.doribottle.web.v1.rental

import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.v1.rental.vm.RentRequest
import com.liah.doribottle.web.v1.rental.vm.RentalSearchRequest
import com.liah.doribottle.web.v1.rental.vm.ReturnRequest
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rental")
class RentalController(
    private val rentalService: RentalService
) {
    @PostMapping
    fun rent(
        @Valid @RequestBody request: RentRequest
    ): UUID {
        return rentalService
            .rent(currentUserId()!!, request.cupRfid!!, request.machineId!!, request.withIce!!)
    }

    @PostMapping("/return")
    fun `return`(
        @Valid @RequestBody request: ReturnRequest
    ) {
        rentalService
            .`return`(request.machineId!!, request.cupRfid!!)
    }

    @GetMapping
    fun getAll(
        @ParameterObject request: RentalSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ) {
        val result = rentalService.getAll(
            userId = currentUserId()!!,
            status = request.status,
            pageable = pageable
        ).map { it }

    }
}