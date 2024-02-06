package com.liah.doribottle.web.v1.rental

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.web.v1.rental.vm.RentRequest
import com.liah.doribottle.web.v1.rental.vm.RentalSearchRequest
import com.liah.doribottle.web.v1.rental.vm.RentalSearchResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/rental")
class RentalController(
    private val rentalService: RentalService
) {
    @Operation(summary = "대여 요청")
    @PostMapping
    fun rent(
        @Valid @RequestBody request: RentRequest
    ): UUID {
        return rentalService.rent(
            userId = currentUserId()!!,
            cupRfid = request.cupRfid!!,
            fromMachineNo = request.machineNo!!,
            withIce = request.withIce!!
        )
    }

    @Operation(summary = "대여 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: RentalSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<RentalSearchResponse> {
        val result = rentalService.getAll(
            userId = currentUserId()!!,
            status = request.status,
            pageable = pageable
        ).map { it.toUserResponse() }

        return CustomPage.of(result)
    }
}