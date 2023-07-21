package com.liah.doribottle.web.v1.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class PointControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/point"

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var pointRepository: PointRepository

    private lateinit var user: User

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
    }

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
        pointRepository.deleteAll()
    }

    @DisplayName("잔여 포인트 조회")
    @Test
    fun getRemainPoint() {
        pointRepository.save(Point(user.id, PointSaveType.REWARD, PointEventType.SAVE_REGISTER_REWARD, 10))
        pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)

        mockMvc.perform(
            MockMvcRequestBuilders.get("$endPoint/remain-point")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("payPoint", `is`(10)))
            .andExpect(jsonPath("freePoint", `is`(10)))
    }
}