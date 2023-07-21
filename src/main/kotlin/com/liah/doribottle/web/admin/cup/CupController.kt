package com.liah.doribottle.web.admin.cup

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.cup.CupService
import com.liah.doribottle.web.admin.cup.vm.CupRegisterRequest
import com.liah.doribottle.web.admin.cup.vm.CupSearchRequest
import com.liah.doribottle.web.admin.cup.vm.CupSearchResponse
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/admin/api/cup")
class CupController(
    private val cupService: CupService
) {
    @PostMapping
    fun register(
        @Valid @RequestBody request: CupRegisterRequest
    ): UUID {
        return cupService.register(
            rfid = request.rfid!!
        )
    }

    @GetMapping
    fun getAll(
        @ParameterObject request: CupSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<CupSearchResponse> {
        val result = cupService.getAll(
            status = request.status,
            pageable = pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }
}