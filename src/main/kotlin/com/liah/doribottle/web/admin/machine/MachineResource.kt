package com.liah.doribottle.web.admin.machine

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.web.admin.machine.vm.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/machine")
class MachineResource(
    private val machineService: MachineService
) {
    @Operation(summary = "기기 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: MachineRegisterRequest
    ): UUID {
        return machineService.register(
            no = request.no!!,
            name = request.name!!,
            type = request.type!!,
            address = request.address!!,
            capacity = request.capacity!!
        )
    }

    @Operation(summary = "기기 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): MachineResponse {
        return machineService.get(id).toResponse()
    }

    @Operation(summary = "기기 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: MachineSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<MachineSearchResponse> {
        val result = machineService.getAll(
                no = request.no,
                name = request.name,
                type = request.type,
                state = request.state,
                addressKeyword = request.addressKeyword,
                pageable = pageable
            ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

    @Operation(summary = "기기 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MachineUpdateRequest
    ) {
        machineService.update(
            id = id,
            name = request.name!!,
            address = request.address!!,
            capacity = request.capacity!!,
            cupAmounts = request.cupAmounts!!
        )
    }

    @Operation(summary = "기기 컵 개수 수정")
    @PutMapping("/{id}/cup-amounts")
    fun updateCupAmounts(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MachineCupAmountsUpdateRequest
    ) {
        machineService.updateCupAmounts(
            id = id,
            cupAmounts = request.cupAmounts!!
        )
    }
}