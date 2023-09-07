package com.liah.doribottle.domain.notification

import com.liah.doribottle.domain.notification.NotificationType.*
import java.util.*

class NotificationIndividual(
    val userId: UUID,
    val type: NotificationType,
    val targetId: UUID? = null,
    vararg args: String?
) {
    val content: String = generateContent(type, *args)

    private fun generateContent(
        type: NotificationType,
        vararg args: String?
    ): String {
        return when(type) {
            POINT -> "${args[0]} 버블 ${args[1]}개가 지급되었습니다."
            REFUND -> "버블 ${args[0]}개 환불 요청이 처리되었습니다."
            NOTICE -> "새로운 공지사항이 등록되었습니다."
            PROMOTION -> "새로운 프로모션이 등록되었습니다."
            PENALTY -> "'${args[0]}'의 사유로 레드카드가 부여되었습니다."
            LOST_CUP -> "컵의 반납 기한이 초과하여 분실 처리되었습니다."
        }
    }
}