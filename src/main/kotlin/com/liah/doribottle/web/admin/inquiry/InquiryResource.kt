package com.liah.doribottle.web.admin.inquiry

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.inquiry.InquiryService
import com.liah.doribottle.service.inquiry.dto.InquiryDto
import com.liah.doribottle.web.admin.inquiry.vm.InquirySearchRequest
import com.liah.doribottle.web.admin.inquiry.vm.InquirySucceedRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/api/inquiry")
class InquiryResource(
    private val inquiryService: InquiryService
) {
    @Operation(summary = "문의 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: InquirySearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<InquiryDto> {
        val result = inquiryService.getAll(
            userId = request.userId,
            type = request.type,
            status = request.status,
            keyword = request.keyword,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "문의 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): InquiryDto {
        return inquiryService.get(id)
    }

    @Operation(summary = "문의 답변")
    @PostMapping("/{id}/succeed")
    fun succeed(
        @PathVariable id: UUID,
        @Valid @RequestBody request: InquirySucceedRequest
    ) {
        inquiryService.succeed(
            id = id,
            answer = request.answer
        )
    }
}