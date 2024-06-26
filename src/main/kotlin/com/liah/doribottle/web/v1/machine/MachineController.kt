package com.liah.doribottle.web.v1.machine

import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.web.v1.machine.vm.MachineResponse
import com.liah.doribottle.web.v1.machine.vm.MachineSimpleResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/machine")
class MachineController(
    private val machineService: MachineService
) {
    @Operation(summary = "기기 전체 조회")
    @GetMapping("/all")
    fun getAll(): List<MachineSimpleResponse> {
        return machineService.getAllSimple(
            pageable = Pageable.unpaged()
        ).content.map { it.toResponse() }
    }

    @Operation(summary = "기기 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): MachineResponse {
        return machineService.get(id).toResponse()
    }
}