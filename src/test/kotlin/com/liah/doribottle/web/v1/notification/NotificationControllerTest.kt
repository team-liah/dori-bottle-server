package com.liah.doribottle.web.v1.notification

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.notification.Alert
import com.liah.doribottle.domain.notification.Notification
import com.liah.doribottle.domain.notification.NotificationType.PENALTY
import com.liah.doribottle.domain.notification.NotificationType.POINT
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.notification.NotificationRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

class NotificationControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/notification"

    @Autowired
    private lateinit var notificationRepository: NotificationRepository
    @Autowired
    private lateinit var alertRepository: AlertRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
        notificationRepository.deleteAll()
    }

    @DisplayName("알림 조회")
    @Test
    fun getAll() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        insertNotifications(user.id)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectTypeValue = listOf(POINT.name, PENALTY.name, PENALTY.name)
        val expectUserIdValue = listOf(user.id.toString(), user.id.toString(), user.id.toString())
        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].type", `is`(expectTypeValue)))
            .andExpect(jsonPath("content[*].userId", `is`(expectUserIdValue)))

        val alertCount = alertRepository.findByIdOrNull(user.id.toString())?.count
        assertThat(alertCount).isNull()
    }

    private fun insertNotifications(userId: UUID) {
        notificationRepository.save(Notification(userId, POINT, "Test", "test", null))
        notificationRepository.save(Notification(UUID.randomUUID(), POINT, "Test", "test", null))
        notificationRepository.save(Notification(userId, PENALTY, "Test", "test", null))
        notificationRepository.save(Notification(userId, PENALTY, "Test", "test", null))
        notificationRepository.save(Notification(userId, PENALTY, "Test", "test", null))
        notificationRepository.save(Notification(UUID.randomUUID(), POINT, "Test", "test", null))
        notificationRepository.save(Notification(userId, POINT, "Test", "test", null))

        alertRepository.save(Alert(userId = userId.toString(), 7))
    }

    @DisplayName("알림 확인")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun read() {
        val notification = notificationRepository.save(Notification(UUID.randomUUID(), POINT, "Test", "test", null))

        mockMvc.perform(
            put("${endPoint}/${notification.id}/read")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }
}