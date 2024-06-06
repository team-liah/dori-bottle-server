package com.liah.doribottle.web.admin.cup

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.cup.CupService
import com.liah.doribottle.service.cup.dto.CupDto
import com.liah.doribottle.service.cup.dto.CupRevisionDto
import com.liah.doribottle.web.admin.cup.vm.CupPatchRequest
import com.liah.doribottle.web.admin.cup.vm.CupRegisterRequest
import com.liah.doribottle.web.admin.cup.vm.CupSearchRequest
import com.liah.doribottle.web.admin.cup.vm.CupUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.history.RevisionSort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/admin/api/cup")
class CupResource(
    private val cupService: CupService,
) {
    @Operation(summary = "컵 등록")
    @PostMapping
    private fun register(
        @Valid @RequestBody request: CupRegisterRequest,
    ): UUID {
        return cupService.register(
            rfid = request.rfid!!,
        )
    }

    @Operation(summary = "컵 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: CupSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable,
    ): CustomPage<CupDto> {
        val result =
            cupService.getAll(
                rfid = request.rfid,
                status = request.status,
                deleted = false,
                pageable = pageable,
            )

        return CustomPage.of(result)
    }

    @Operation(summary = "컵 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID,
    ): CupDto {
        return cupService.get(id)
    }

    @Operation(summary = "컵 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: CupUpdateRequest,
    ) {
        cupService.update(
            id = id,
            rfid = request.rfid!!,
            status = request.status!!,
        )
    }

    @Operation(summary = "컵 패치")
    @PatchMapping("/{id}")
    fun patch(
        @PathVariable id: UUID,
        @RequestBody request: CupPatchRequest,
    ) {
        val cup = cupService.get(id)
        cupService.update(
            id = id,
            rfid = request.rfid ?: cup.rfid,
            status = request.status ?: cup.status,
        )
    }

    @Operation(summary = "컵 삭제")
    @DeleteMapping("/{id}")
    fun remove(
        @PathVariable id: UUID,
    ) {
        cupService.remove(id)
    }

    @Operation(summary = "컵 수정 이력 조회")
    @GetMapping("/{id}/revisions")
    fun getAllRevisions(
        @PathVariable id: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") size: Int,
        @RequestParam(required = false, defaultValue = "DESC") direction: Sort.Direction,
    ): CustomPage<CupRevisionDto> {
        val pageable =
            when (direction) {
                Sort.Direction.ASC -> PageRequest.of(page, size, RevisionSort.asc())
                Sort.Direction.DESC -> PageRequest.of(page, size, RevisionSort.desc())
            }

        return CustomPage.of(cupService.getAllRevisions(id, pageable))
    }
}
