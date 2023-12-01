package com.liah.doribottle.web.admin.inquiry

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.inquiry.Inquiry
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.inquiry.InquiryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.inquiry.vm.InquirySucceedRequest
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

class InquiryResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/inquiry"

    @Autowired
    private lateinit var inquiryRepository: InquiryRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    internal fun destroy() {
        inquiryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("문의 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        val userA = userRepository.save(User("010-1111-1111", "A", "010-1111-1111", Role.USER))
        val userB = userRepository.save(User("010-2222-2222", "B", "010-2222-2222", Role.USER))
        val userC = userRepository.save(User("010-3333-3333", "C", "010-3333-3333", Role.USER))
        insertInquiries(userA, userB, userC)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "6")

        val expectUserId = listOf("${userB.id}", "${userC.id}", "${userA.id}", "${userC.id}", "${userB.id}", "${userA.id}",)
        val expectType = listOf(InquiryType.ETC.name, InquiryType.ETC.name, InquiryType.ETC.name, InquiryType.ETC.name, InquiryType.ETC.name, InquiryType.REFUND.name)
        val expectBankAccount = listOf("국민")
        val expectContent = listOf("6", "5", "4", "3", "2", "1")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].user.id", `is`(expectUserId)))
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].bankAccount.bank", `is`(expectBankAccount)))
            .andExpect(jsonPath("content[*].content", `is`(expectContent)))
    }

    private fun insertInquiries(userA: User, userB: User, userC: User) {
        val bankAccount = BankAccountDto("국민", "943202-00-120364", "김동준")
        val inquiry1 = Inquiry(userA, InquiryType.REFUND, bankAccount.toEmbeddable(), "1")
        inquiry1.succeed("환불 완료")
        inquiryRepository.save(inquiry1)
        inquiryRepository.save(Inquiry(userB, InquiryType.ETC, null, "2"))
        inquiryRepository.save(Inquiry(userC, InquiryType.ETC, null, "3"))
        inquiryRepository.save(Inquiry(userA, InquiryType.ETC, null, "4"))
        inquiryRepository.save(Inquiry(userC, InquiryType.ETC, null, "5"))
        inquiryRepository.save(Inquiry(userB, InquiryType.ETC, null, "6"))
    }

    @DisplayName("문의 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val user = userRepository.save(User("010-1111-1111", "A", "010-1111-1111", Role.USER))
        val bankAccount = BankAccountDto("국민", "943202-00-120364", "김동준")
        val inquiry = Inquiry(user, InquiryType.REFUND, bankAccount.toEmbeddable(), "1")
        inquiry.succeed("환불 완료")
        inquiryRepository.save(inquiry)

        mockMvc.perform(
            get("${endPoint}/${inquiry.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("user.id", `is`(user.id.toString())))
            .andExpect(jsonPath("type", `is`(inquiry.type.name)))
            .andExpect(jsonPath("bankAccount.bank", `is`("국민")))
            .andExpect(jsonPath("bankAccount.accountNumber", `is`("943202-00-120364")))
            .andExpect(jsonPath("bankAccount.accountHolder", `is`("김동준")))
            .andExpect(jsonPath("content", `is`("1")))
    }

    @DisplayName("문의 답변")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun succeed() {
        val user = userRepository.save(User("010-1111-1111", "A", "010-1111-1111", Role.USER))
        val bankAccount = BankAccountDto("국민", "943202-00-120364", "김동준")
        val inquiry = Inquiry(user, InquiryType.REFUND, bankAccount.toEmbeddable(), "1")
        inquiry.succeed("환불 완료")
        inquiryRepository.save(inquiry)

        val body = InquirySucceedRequest("환불 완료")

        mockMvc.perform(
            post("$endPoint/${inquiry.id}/succeed")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }
}