package com.liah.doribottle.service.inquiry

import com.liah.doribottle.domain.inquiry.BankAccount
import com.liah.doribottle.domain.inquiry.Inquiry
import com.liah.doribottle.domain.inquiry.InquiryStatus.PROCEEDING
import com.liah.doribottle.domain.inquiry.InquiryStatus.SUCCEEDED
import com.liah.doribottle.domain.inquiry.InquiryType.REFUND
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.inquiry.InquiryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class InquiryServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var inquiryService: InquiryService
    @Autowired
    private lateinit var inquiryRepository: InquiryRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("문의 등록")
    @Test
    fun register() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        clear()

        //when
        val id = inquiryService.register(user.id, REFUND, BankAccountDto("국민", "943202-00-120364", "김동준"), "버블 환불")
        clear()

        //then
        val findInquiry = inquiryRepository.findByIdOrNull(id)
        assertThat(findInquiry?.user?.id).isEqualTo(user.id)
        assertThat(findInquiry?.type).isEqualTo(REFUND)
        assertThat(findInquiry?.bankAccount?.bank).isEqualTo("국민")
        assertThat(findInquiry?.bankAccount?.accountNumber).isEqualTo("943202-00-120364")
        assertThat(findInquiry?.bankAccount?.accountHolder).isEqualTo("김동준")
        assertThat(findInquiry?.content).isEqualTo("버블 환불")
        assertThat(findInquiry?.answer).isNull()
        assertThat(findInquiry?.status).isEqualTo(PROCEEDING)
    }

    @DisplayName("문의 목록 조회")
    @Test
    fun getAll() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        insertInquiries(user)
        clear()

        //when
        val result = inquiryService.getAll(pageable = Pageable.ofSize(3))

        //then
        assertThat(result)
            .extracting("user.id")
            .containsExactly(user.id, user.id, user.id)
        assertThat(result)
            .extracting("content")
            .containsExactly("1", "2", "3")
        assertThat(result)
            .extracting("status")
            .containsExactly(SUCCEEDED, PROCEEDING, PROCEEDING)
        assertThat(result)
            .extracting("type")
            .containsExactly(REFUND, REFUND, REFUND)
    }

    private fun insertInquiries(user: User) {
        val bankAccount = BankAccountDto("국민", "943202-00-120364", "김동준")
        val inquiry1 = Inquiry(user, REFUND, bankAccount.toEmbeddable(), "1")
        inquiry1.succeed("환불 완료")
        inquiryRepository.save(inquiry1)
        inquiryRepository.save(Inquiry(user, REFUND, bankAccount.toEmbeddable(), "2"))
        inquiryRepository.save(Inquiry(user, REFUND, bankAccount.toEmbeddable(), "3"))
        inquiryRepository.save(Inquiry(user, REFUND, bankAccount.toEmbeddable(), "4"))
        inquiryRepository.save(Inquiry(user, REFUND, bankAccount.toEmbeddable(), "5"))
        inquiryRepository.save(Inquiry(user, REFUND, bankAccount.toEmbeddable(), "6"))
    }

    @DisplayName("문의 단건 조회")
    @Test
    fun get() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val inquiry = inquiryRepository.save(Inquiry(user, REFUND, BankAccount("국민", "943202-00-120364", "김동준"), "test"))
        clear()

        //when
        val result = inquiryService.get(inquiry.id)

        //then
        assertThat(result.user.id).isEqualTo(user.id)
        assertThat(result.type).isEqualTo(REFUND)
        assertThat(result.bankAccount?.bank).isEqualTo("국민")
        assertThat(result.bankAccount?.accountNumber).isEqualTo("943202-00-120364")
        assertThat(result.bankAccount?.accountHolder).isEqualTo("김동준")
        assertThat(result.content).isEqualTo("test")
        assertThat(result.answer).isNull()
        assertThat(result.status).isEqualTo(PROCEEDING)
    }

    @DisplayName("문의 답변")
    @Test
    fun succeed() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val inquiry = inquiryRepository.save(Inquiry(user, REFUND, BankAccount("국민", "943202-00-120364", "김동준"), "test"))
        clear()

        //when
        inquiryService.succeed(inquiry.id, "환불 완료")
        clear()

        //then
        val findInquiry = inquiryRepository.findByIdOrNull(inquiry.id)
        assertThat(findInquiry?.user?.id).isEqualTo(user.id)
        assertThat(findInquiry?.type).isEqualTo(REFUND)
        assertThat(findInquiry?.bankAccount?.bank).isEqualTo("국민")
        assertThat(findInquiry?.bankAccount?.accountNumber).isEqualTo("943202-00-120364")
        assertThat(findInquiry?.bankAccount?.accountHolder).isEqualTo("김동준")
        assertThat(findInquiry?.content).isEqualTo("test")
        assertThat(findInquiry?.answer).isEqualTo("환불 완료")
        assertThat(findInquiry?.status).isEqualTo(SUCCEEDED)
    }
}