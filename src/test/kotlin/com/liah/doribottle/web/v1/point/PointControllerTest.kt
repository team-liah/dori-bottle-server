package com.liah.doribottle.web.v1.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.point.PointHistoryRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class PointControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/point"

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var pointHistoryRepository: PointHistoryRepository

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
        pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)

        mockMvc.perform(
            get("$endPoint/remain-point")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("payPoint", `is`(10)))
            .andExpect(jsonPath("freePoint", `is`(10)))
    }

    @DisplayName("포인트 내역 조회")
    @Test
    fun getAllHistories() {
        insertHistories()

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectUserIds = listOf(user.id.toString(), user.id.toString(), user.id.toString())
        val expectEventType = listOf(USE_CUP.name, SAVE_PAY.name, SAVE_REGISTER_REWARD.name)
        val expectAmounts = listOf(-2, 10, 10)

        mockMvc.perform(
            get("${endPoint}/history")
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].userId", `is`(expectUserIds)))
            .andExpect(jsonPath("content[*].eventType", `is`(expectEventType)))
            .andExpect(jsonPath("content[*].amounts", `is`(expectAmounts)))
    }

    private fun insertHistories() {
        pointHistoryRepository.save(PointHistory(user.id, SAVE_REGISTER_REWARD, 10))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, USE_CUP, -2))
    }
}