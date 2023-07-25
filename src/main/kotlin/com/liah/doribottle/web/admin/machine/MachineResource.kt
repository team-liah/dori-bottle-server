package com.liah.doribottle.web.admin.machine

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.web.admin.machine.vm.MachineRegisterRequest
import com.liah.doribottle.web.admin.machine.vm.MachineSearchRequest
import com.liah.doribottle.web.admin.machine.vm.MachineSearchResponse
import com.liah.doribottle.web.admin.machine.vm.MachineUpdateRequest
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

    @GetMapping
    fun getAll(
        @ParameterObject request: MachineSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<MachineSearchResponse> {
        val result = machineService.getAll(
                name = request.name,
                type = request.type,
                state = request.state,
                addressKeyword = request.addressKeyword,
                pageable = pageable
            ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

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
}