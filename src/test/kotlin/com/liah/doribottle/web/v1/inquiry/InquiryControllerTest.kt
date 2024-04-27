package com.liah.doribottle.web.v1.inquiry

import com.liah.doribottle.domain.inquiry.Inquiry
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.inquiry.InquiryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.inquiry.vm.InquiryRegisterRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class InquiryControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/inquiry"

    @Autowired
    private lateinit var inquiryRepository: InquiryRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    internal fun destroy() {
        inquiryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("문의 등록")
    @Test
    fun register() {
        // given
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val body = InquiryRegisterRequest(InquiryType.REFUND, null, null, null, null)

        // when, then
        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("문의 목록 조회")
    @Test
    fun getAll() {
        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        insertInquiries(user)

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "6")

        val expectType =
            listOf(
                InquiryType.ETC.name,
                InquiryType.ETC.name,
                InquiryType.ETC.name,
                InquiryType.ETC.name,
                InquiryType.ETC.name,
                InquiryType.REFUND.name,
            )
        val expectBankAccount = listOf("국민")
        val expectContent = listOf("6", "5", "4", "3", "2", "1")
        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].bankAccount.bank", `is`(expectBankAccount)))
            .andExpect(jsonPath("content[*].content", `is`(expectContent)))
    }

    private fun insertInquiries(user: User) {
        val bankAccount = BankAccountDto("국민", "943202-00-120364", "김동준")
        val inquiry1 = Inquiry(user, InquiryType.REFUND, bankAccount.toEmbeddable(), "1")
        inquiry1.succeed("환불 완료")
        inquiryRepository.save(inquiry1)
        inquiryRepository.save(Inquiry(user, InquiryType.ETC, null, "2"))
        inquiryRepository.save(Inquiry(user, InquiryType.ETC, null, "3"))
        inquiryRepository.save(Inquiry(user, InquiryType.ETC, null, "4"))
        inquiryRepository.save(Inquiry(user, InquiryType.ETC, null, "5"))
        inquiryRepository.save(Inquiry(user, InquiryType.ETC, null, "6"))
    }
}
