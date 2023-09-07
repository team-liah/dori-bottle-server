package com.liah.doribottle.service.notification

import com.liah.doribottle.domain.notification.Alert
import com.liah.doribottle.domain.notification.Notification
import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.domain.notification.NotificationType.*
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.user.PenaltyType
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.notification.NotificationRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class NotificationServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var notificationService: NotificationService
    @Autowired
    private lateinit var notificationRepository: NotificationRepository
    @Autowired
    private lateinit var alertRepository: AlertRepository

    @DisplayName("알림 저장")
    @Test
    fun saveAll() {
        //given, when
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()
        val userId4 = UUID.randomUUID()

        val individuals = listOf(
            NotificationIndividual(userId1, POINT, null, PointEventType.SAVE_REGISTER_REWARD.title, "10"),
            NotificationIndividual(userId2, REFUND, null, "20"),
            NotificationIndividual(userId3, LOST_CUP),
            NotificationIndividual(userId4, PENALTY, null, PenaltyType.DAMAGED_CUP.title)
        )

        //when
        val ids = notificationService.saveAll(individuals)
        clear()

        //then
        val findNotifications = notificationRepository.findAllById(ids)
        assertThat(findNotifications)
            .extracting("userId")
            .containsExactly(userId1, userId2, userId3, userId4)
        assertThat(findNotifications)
            .extracting("type")
            .containsExactly(POINT, REFUND, LOST_CUP, PENALTY)
        assertThat(findNotifications)
            .extracting("title")
            .containsExactly(POINT.title, REFUND.title, LOST_CUP.title, PENALTY.title)
        assertThat(findNotifications)
            .extracting("content")
            .containsExactly(
                "회원가입 보상 버블 10개가 지급되었습니다.",
                "버블 20개 환불 요청이 처리되었습니다.",
                "컵의 반납 기한이 초과하여 분실 처리되었습니다.",
                "'파손된 컵 반납'의 사유로 레드카드가 부여되었습니다."
            )
    }

    @DisplayName("알림 조회")
    @Test
    fun getAll() {
        //given
        val userId = UUID.randomUUID()
        insertNotifications(userId)
        clear()

        //when
        val result = notificationService.getAll(
            userId = userId,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("userId")
            .containsExactly(userId, userId, userId)
        assertThat(result)
            .extracting("type")
            .containsExactly(POINT, NOTICE, PROMOTION)
    }

    private fun insertNotifications(userId: UUID) {
        notificationRepository.save(Notification(userId, POINT, "Test", "test", null))
        notificationRepository.save(Notification(UUID.randomUUID(), POINT, "Test", "test", null))
        notificationRepository.save(Notification(userId, NOTICE, "Test", "test", null))
        notificationRepository.save(Notification(userId, PROMOTION, "Test", "test", null))
        notificationRepository.save(Notification(userId, NOTICE, "Test", "test", null))
        notificationRepository.save(Notification(userId, POINT, "Test", "test", null))
    }

    @DisplayName("알림 확인")
    @Test
    fun read() {
        //given
        val userId = UUID.randomUUID()
        val notification = notificationRepository.save(Notification(userId, POINT, "Test", "test", null))
        clear()

        //when
        notificationService.read(notification.id)
        clear()

        //then
        val findNotification = notificationRepository.findByIdOrNull(notification.id)
        assertThat(findNotification?.read).isTrue
    }

    @DisplayName("Alert")
    @Test
    fun alert() {
        //given
        val userId = UUID.randomUUID()
        val userIdString = userId.toString()

        //when
        notificationService.alert(userId)

        //then
        val alert = alertRepository.findByIdOrNull(userIdString)
        assertThat(alert?.userId).isEqualTo(userIdString)
        assertThat(alert?.count).isEqualTo(1)
    }

    @DisplayName("Alert TC2")
    @Test
    fun alertTc2() {
        //given
        val userId = UUID.randomUUID()
        val userIdString = userId.toString()
        alertRepository.save(Alert(userIdString, 3))

        //when
        notificationService.alert(userId)

        //then
        val alert = alertRepository.findByIdOrNull(userIdString)
        assertThat(alert?.userId).isEqualTo(userIdString)
        assertThat(alert?.count).isEqualTo(4)
    }

    @DisplayName("Clear Alert")
    @Test
    fun clearAlert() {
        //given
        val userId = UUID.randomUUID()
        val userIdString = userId.toString()
        alertRepository.save(Alert(userIdString, 3))

        //when
        notificationService.clearAlert(userId)

        //then
        val alert = alertRepository.findByIdOrNull(userIdString)
        assertThat(alert).isNull()
    }

    @DisplayName("Get Alert Count")
    @Test
    fun getAlertCount() {
        //given
        val userId = UUID.randomUUID()
        val userIdString = userId.toString()
        alertRepository.save(Alert(userIdString, 3))

        //when
        val alertCount = notificationService.getAlertCount(userId)

        //then
        assertThat(alertCount).isEqualTo(3)
    }

    @DisplayName("Get Alert Count TC2")
    @Test
    fun getAlertCountTc2() {
        //given
        val userId = UUID.randomUUID()

        //when
        val alertCount = notificationService.getAlertCount(userId)

        //then
        assertThat(alertCount).isEqualTo(0)
    }
}