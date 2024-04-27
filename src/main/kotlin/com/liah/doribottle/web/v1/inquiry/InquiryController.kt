package com.liah.doribottle.web.v1.inquiry

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.inquiry.InquiryService
import com.liah.doribottle.web.v1.inquiry.vm.InquiryRegisterRequest
import com.liah.doribottle.web.v1.inquiry.vm.InquirySearchResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/inquiry")
class InquiryController(
    private val inquiryService: InquiryService,
) {
    @Operation(summary = "문의 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: InquiryRegisterRequest,
    ) {
        inquiryService.register(
            userId = currentUserId()!!,
            type = request.type!!,
            bankAccount = request.bankAccountDto,
            content = request.content,
            target = request.target,
            imageUrls = request.imageUrls,
        )
    }

    @Operation(summary = "문의 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = DESC) pageable: Pageable,
    ): CustomPage<InquirySearchResponse> {
        val result =
            inquiryService.getAll(
                userId = currentUserId()!!,
                pageable = pageable,
            ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }
}
