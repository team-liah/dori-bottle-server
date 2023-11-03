package com.liah.doribottle.web.admin.asset

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.s3.AwsS3Service
import com.liah.doribottle.service.s3.dto.AwsS3UploadResultDto
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AssetControllerTest : BaseControllerTest() {
    private val endPoint = "/admin/api/asset"

    @MockBean
    private lateinit var awsS3Service: AwsS3Service

    @DisplayName("에셋 업로드")
    @WithMockDoriUser(loginId = "admin", role = Role.ADMIN)
    @Test
    fun upload() {
        //given
        val file = MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".toByteArray())
        given(awsS3Service.uploadWithPublicRead(file, "admin"))
            .willReturn(AwsS3UploadResultDto("dummyKey", "dummyUrl"))

        //when, then
        mockMvc.perform(
            multipart("${endPoint}/upload")
                .file(file)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("key", `is`("dummyKey")))
            .andExpect(jsonPath("url", `is`("dummyUrl")))

        verify(awsS3Service, times(1))
            .uploadWithPublicRead(eq(file), eq("admin"))
    }
}