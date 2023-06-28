package com.liah.doribottle.web.account

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.web.account.vm.AuthRequest
import com.liah.doribottle.web.account.vm.SendSmsRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@SpringBootTest
class AccountControllerTest {
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var mockMvc: MockMvc
    private val endPoint = "/api/v1/account"

    private val userLoginId = "01056383316"
    private val guestLoginId = "01012345678"
    private lateinit var user: User
    private lateinit var guest: User

    @BeforeEach
    internal fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply<DefaultMockMvcBuilder?>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
            .build()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(userLoginId, "Tester 1", userLoginId, Role.USER)
        userEntity.updatePassword(passwordEncoder.encode("123456"))
        user = userRepository.save(userEntity)

        guest = userRepository.save(User(guestLoginId, "Tester 2", guestLoginId, Role.GUEST))
    }

    @DisplayName("인증요청")
//    @Test
    fun sendSms() {
        val body = SendSmsRequest(userLoginId)

        mockMvc.perform(
            post("$endPoint/auth/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                document(
                    "account/auth/send-sms",
                    requestFields(
                        fieldWithPath("loginId").description("Login ID (User's phone number)")
                    )
                )
            )
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        val body = AuthRequest(userLoginId, "123456")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(
                document(
                    "account/auth",
                    requestFields(
                        fieldWithPath("loginId").description("Login ID (User's phone number)"),
                        fieldWithPath("loginPassword").description("Login Password (Received SMS Text)")
                    )
                )
            )
    }
}