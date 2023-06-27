package com.liah.doribottle.web

import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(RestDocumentationExtension::class, SpringExtension::class)
@SpringBootTest
class SampleControllerTest {
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    internal fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentation: RestDocumentationContextProvider
    ) {
       mockMvc = MockMvcBuilders
           .webAppContextSetup(webApplicationContext)
           .apply<DefaultMockMvcBuilder?>(documentationConfiguration(restDocumentation))
           .build()
    }

    @DisplayName("샘플 API 테스트")
    @Test
    fun getSampleByIdTest() {
        mockMvc.perform(
            RestDocumentationRequestBuilders.get("/api/v1/sample/{id}", 1).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`("1")))
            .andExpect(jsonPath("name", `is`("test-1")))
            .andDo(
                document(
                "index",
                    pathParameters(parameterWithName("id").description("The id of the input to find"))
                )
            )
    }
}