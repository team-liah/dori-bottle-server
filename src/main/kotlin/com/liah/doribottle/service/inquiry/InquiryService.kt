package com.liah.doribottle.service.inquiry

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.inquiry.Inquiry
import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.repository.inquiry.InquiryQueryRepository
import com.liah.doribottle.repository.inquiry.InquiryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import com.liah.doribottle.service.inquiry.dto.InquiryDto
import com.liah.doribottle.service.inquiry.dto.InquiryTargetDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class InquiryService(
    private val inquiryRepository: InquiryRepository,
    private val inquiryQueryRepository: InquiryQueryRepository,
    private val userRepository: UserRepository,
) {
    fun register(
        userId: UUID? = null,
        type: InquiryType,
        bankAccount: BankAccountDto? = null,
        content: String? = null,
        target: InquiryTargetDto? = null,
        imageUrls: List<String>? = null,
    ): UUID {
        val user =
            userId?.let {
                userRepository.findByIdOrNull(userId)
                    ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
            }
        val inquiry =
            inquiryRepository.save(
                Inquiry(
                    user = user,
                    type = type,
                    bankAccount = bankAccount?.toEmbeddable(),
                    content = content,
                    target = target?.toEmbeddable(),
                    imageUrls = imageUrls,
                ),
            )

        return inquiry.id
    }

    @Transactional(readOnly = true)
    fun getAll(
        userId: UUID? = null,
        type: InquiryType? = null,
        status: InquiryStatus? = null,
        keyword: String? = null,
        pageable: Pageable,
    ): Page<InquiryDto> {
        return inquiryQueryRepository.getAll(
            userId = userId,
            type = type,
            status = status,
            keyword = keyword,
            pageable = pageable,
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun get(id: UUID): InquiryDto {
        val inquiry =
            inquiryRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.INQUIRY_NOT_FOUNT)

        return inquiry.toDto()
    }

    fun succeed(
        id: UUID,
        answer: String? = null,
    ) {
        val inquiry =
            inquiryRepository.findByIdOrNull(id)
                ?: throw NotFoundException(ErrorCode.INQUIRY_NOT_FOUNT)

        inquiry.succeed(answer)
    }
}
