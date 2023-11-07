package com.liah.doribottle.web.admin.machine

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.web.admin.machine.vm.MachinePatchUpdateRequest
import com.liah.doribottle.web.admin.machine.vm.MachineRegisterRequest
import com.liah.doribottle.web.admin.machine.vm.MachineSearchRequest
import com.liah.doribottle.web.admin.machine.vm.MachineUpdateRequest
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
    ): MachineDto {
        return machineService.get(id)
    }

    @Operation(summary = "기기 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: MachineSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<MachineDto> {
        val result = machineService.getAll(
                no = request.no,
                name = request.name,
                type = request.type,
                state = request.state,
                addressKeyword = request.addressKeyword,
                pageable = pageable
            )

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

    @Operation(summary = "기기 패치")
    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MachinePatchUpdateRequest
    ) {
        val machine = machineService.get(id)
        machineService.update(
            id = id,
            name = request.name ?: machine.name,
            address = request.address ?: machine.address,
            capacity = request.capacity ?: machine.capacity,
            cupAmounts = request.cupAmounts ?: machine.cupAmounts
        )
    }
}