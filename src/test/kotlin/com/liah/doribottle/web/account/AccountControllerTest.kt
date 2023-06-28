package com.liah.doribottle.web.account

import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.web.account.vm.SendSmsRequest
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@SpringBootTest
class AccountControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/api/v1/account"

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

    @DisplayName("인증 요청 테스트")
    @Test
    fun sendSms() {
        val body = SendSmsRequest("01056383316")

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
}