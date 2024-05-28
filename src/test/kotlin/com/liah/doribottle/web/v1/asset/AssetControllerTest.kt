package com.liah.doribottle.web.v1.asset

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.s3.AwsS3Service
import com.liah.doribottle.service.s3.dto.AwsS3UploadResultDto
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AssetControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/asset"

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var awsS3Service: AwsS3Service

    private lateinit var user: User

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.updatePassword(encodePassword("123456"))
        user = userRepository.save(userEntity)
    }

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
    }

    @DisplayName("유저 에셋 업로드")
    @Test
    fun upload() {
        // given
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val file = MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".toByteArray())
        given(awsS3Service.uploadWithPublicRead(file, "${user.id}"))
            .willReturn(AwsS3UploadResultDto("dummyKey", "dummyUrl"))

        // when, then
        mockMvc.perform(
            multipart("$endPoint/upload")
                .file(file)
                .cookie(cookie),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("key", `is`("dummyKey")))
            .andExpect(jsonPath("url", `is`("dummyUrl")))

        verify(awsS3Service, times(1))
            .uploadWithPublicRead(eq(file), eq("${user.id}"))
    }

    @DisplayName("유저 에셋 업로드 - 실패: 인증되지 않은 사용자")
    @Test
    fun upload_Unauthorized() {
        // given
        val file = MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".toByteArray())

        // when, then
        mockMvc.perform(
            multipart("$endPoint/upload")
                .file(file),
        )
            .andExpect(status().isUnauthorized)
    }
}
